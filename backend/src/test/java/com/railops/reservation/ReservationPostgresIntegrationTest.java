package com.railops.reservation;

import static org.assertj.core.api.Assertions.*;

import com.railops.auth.security.UserPrincipal;
import com.railops.common.error.BusinessException;
import com.railops.common.error.ErrorCode;
import com.railops.payment.domain.PaymentStatus;
import com.railops.payment.service.PaymentService;
import com.railops.reservation.domain.ReservationStatus;
import com.railops.reservation.dto.ReservationHoldRequest;
import com.railops.reservation.dto.ReservationHoldResponse;
import com.railops.reservation.service.ReservationQueryService;
import com.railops.reservation.service.ReservationService;
import com.railops.route.domain.Route;
import com.railops.schedule.domain.TrainSchedule;
import com.railops.seat.domain.ScheduleSeat;
import com.railops.seat.domain.ScheduleSeatStatus;
import com.railops.station.domain.Station;
import com.railops.train.domain.*;
import com.railops.user.domain.User;
import jakarta.persistence.EntityManager;
import java.net.URI;
import java.net.http.*;
import java.sql.DriverManager;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(ReservationPostgresIntegrationTest.TimeConfiguration.class)
@EnabledIfEnvironmentVariable(named = "RAILOPS_TEST_DB_URL", matches = ".+")
class ReservationPostgresIntegrationTest {
    private static final String SCHEMA = "railops_test_" + UUID.randomUUID().toString().replace("-", "");
    private static final Instant START = Instant.parse("2035-01-01T00:00:00Z");
    private static String url() { return System.getenv("RAILOPS_TEST_DB_URL"); }
    private static String user() { return System.getenv().getOrDefault("RAILOPS_TEST_DB_USERNAME", "railops"); }
    private static String password() { return System.getenv().getOrDefault("RAILOPS_TEST_DB_PASSWORD", "railops"); }

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry props) throws Exception {
        schemaSql("CREATE SCHEMA " + SCHEMA);
        props.add("spring.datasource.url", () -> url() + (url().contains("?") ? "&" : "?") + "currentSchema=" + SCHEMA);
        props.add("spring.datasource.username", ReservationPostgresIntegrationTest::user);
        props.add("spring.datasource.password", ReservationPostgresIntegrationTest::password);
        props.add("spring.datasource.hikari.connection-init-sql", () -> "SET lock_timeout = '5s'");
        props.add("spring.jpa.hibernate.ddl-auto", () -> "create");
        props.add("spring.jpa.properties.hibernate.default_schema", () -> SCHEMA);
        props.add("spring.jpa.open-in-view", () -> "false");
        props.add("railops.hold.scheduler-enabled", () -> "false");
        props.add("railops.fare.default-seat-price", () -> "50000");
    }

    private static void schemaSql(String sql) throws Exception {
        if (!SCHEMA.matches("railops_test_[a-f0-9]{32}")) throw new IllegalStateException("Invalid test schema");
        try (var c = DriverManager.getConnection(url(), user(), password()); var s = c.createStatement()) { s.execute(sql); }
    }

    @AfterAll static void cleanup() throws Exception { schemaSql("DROP SCHEMA IF EXISTS " + SCHEMA + " CASCADE"); }
    @Autowired ReservationService reservations;
    @Autowired ReservationQueryService queries;
    @Autowired PaymentService payments;
    @Autowired com.railops.seat.service.ScheduleSeatService seatService;
    @Autowired EntityManager em;
    @Autowired PlatformTransactionManager transactions;
    @Autowired MutableClock clock;
    @LocalServerPort int port;
    private Fixture fixture;

    @BeforeEach void setup() {
        clock.set(START);
        fixture = new TransactionTemplate(transactions).execute(status -> {
            String suffix = UUID.randomUUID().toString().substring(0, 8);
            User first = User.createUser(suffix + "@test.local", "unused", "Test User");
            User second = User.createUser("other-" + suffix + "@test.local", "unused", "Other User");
            em.persist(first); em.persist(second);
            Station origin = Station.create("Test Seoul", "S" + suffix, "Seoul");
            Station destination = Station.create("Test Busan", "B" + suffix, "Busan");
            em.persist(origin); em.persist(destination);
            Route route = Route.create("Test Route", origin, destination); em.persist(route);
            Train train = Train.create("T" + suffix, "TEST", "Test Train"); em.persist(train);
            Car car = Car.create(train, 1, 2); em.persist(car);
            Seat a = Seat.create(car, "1A", SeatType.STANDARD), b = Seat.create(car, "1B", SeatType.STANDARD);
            em.persist(a); em.persist(b);
            LocalDateTime departure = LocalDateTime.now(clock).plusDays(1);
            TrainSchedule schedule = TrainSchedule.create(train, route, departure.toLocalDate(), departure, departure.plusHours(2));
            em.persist(schedule);
            ScheduleSeat sa = ScheduleSeat.createAvailable(schedule, a), sb = ScheduleSeat.createAvailable(schedule, b);
            em.persist(sa); em.persist(sb); em.flush();
            return new Fixture(UserPrincipal.from(first), UserPrincipal.from(second), schedule.getId(), sa.getId(), sb.getId());
        });
    }

    @Test void twoConcurrentHoldsHaveExactlyOneWinner() throws Exception {
        var outcomes = race(() -> hold(fixture.first, fixture.a), () -> hold(fixture.second, fixture.a));
        assertThat(outcomes.stream().filter(ReservationHoldResponse.class::isInstance).count()).isEqualTo(1);
        assertThat(outcomes).contains(ErrorCode.SEAT_ALREADY_HELD);
    }

    @Test void reverseSeatOrderDoesNotDeadlockAndAmountsAreSnapshots() throws Exception {
        var outcomes = race(() -> hold(fixture.first, fixture.a, fixture.b), () -> hold(fixture.second, fixture.b, fixture.a));
        var winner = outcomes.stream().filter(ReservationHoldResponse.class::isInstance).map(ReservationHoldResponse.class::cast).findFirst().orElseThrow();
        assertThat(winner.amount()).isEqualByComparingTo("100000");
        assertThat(queries.detail(null, winner.reservationId()).seats()).hasSize(2)
            .allSatisfy(s -> assertThat(s.price()).isEqualByComparingTo("50000"));
        assertThat(outcomes).contains(ErrorCode.SEAT_ALREADY_HELD);
    }

    @Test void duplicateSuccessAndCancelAreIdempotentAndRefundOnce() throws Exception {
        var h = hold(fixture.first, fixture.a);
        var paid = race(() -> payments.simulateSuccess(fixture.first, h.paymentId()), () -> payments.simulateSuccess(fixture.first, h.paymentId()));
        assertThat(paid).noneMatch(ErrorCode.class::isInstance);
        var canceled = race(() -> payments.cancelReservation(fixture.first, h.reservationId()), () -> payments.cancelReservation(fixture.first, h.reservationId()));
        assertThat(canceled).noneMatch(ErrorCode.class::isInstance);
        var detail = queries.getMine(fixture.first, h.reservationId());
        assertThat(detail.payment().status()).isEqualTo(PaymentStatus.REFUNDED);
        assertThat(detail.payment().refundedAmount()).isEqualByComparingTo("50000");
        assertThat(seatState(fixture.a)).isEqualTo(ScheduleSeatStatus.AVAILABLE);
    }

    @Test void simultaneousPaymentAndCancellationRemainConsistent() throws Exception {
        var h = hold(fixture.first, fixture.a);
        race(() -> payments.simulateSuccess(fixture.first, h.paymentId()), () -> payments.cancelReservation(fixture.first, h.reservationId()));
        var d = queries.getMine(fixture.first, h.reservationId());
        assertThat(d.reservation().status()).isEqualTo(ReservationStatus.CANCELED);
        assertThat(d.payment().status()).isIn(PaymentStatus.CANCELED, PaymentStatus.REFUNDED);
        assertThat(seatState(fixture.a)).isEqualTo(ScheduleSeatStatus.AVAILABLE);
    }

    @Test void expiredRejectionCommits() {
        var h = hold(fixture.first, fixture.a);
        clock.set(START.plusSeconds(600));
        assertThatThrownBy(() -> payments.simulateSuccess(fixture.first, h.paymentId()))
            .isInstanceOf(BusinessException.class).extracting("errorCode").isEqualTo(ErrorCode.HOLD_EXPIRED);
        assertThat(queries.getMine(fixture.first, h.reservationId()).payment().status()).isEqualTo(PaymentStatus.EXPIRED);
        assertThat(seatState(fixture.a)).isEqualTo(ScheduleSeatStatus.AVAILABLE);
    }

    @Test void oldCleanupCannotReleaseSameUsersNewHold() {
        var old = hold(fixture.first, fixture.a);
        clock.set(START.plusSeconds(601));
        var next = hold(fixture.first, fixture.a);
        assertThat(payments.expireIfDue(old.paymentId())).isTrue();
        assertThat(seatState(fixture.a)).isEqualTo(ScheduleSeatStatus.HELD);
        payments.simulateSuccess(fixture.first, next.paymentId());
        assertThat(seatState(fixture.a)).isEqualTo(ScheduleSeatStatus.RESERVED);
    }

    @Test void expiryAndSuccessAtBoundaryCannotConfirmExpiredBooking() throws Exception {
        var h = hold(fixture.first, fixture.a);
        clock.set(START.plusSeconds(600));
        race(() -> payments.expireIfDue(h.paymentId()), () -> payments.simulateSuccess(fixture.first, h.paymentId()));
        var d = queries.getMine(fixture.first, h.reservationId());
        assertThat(d.payment().status()).isEqualTo(PaymentStatus.EXPIRED);
        assertThat(d.reservation().status()).isEqualTo(ReservationStatus.EXPIRED);
        assertThat(seatState(fixture.a)).isEqualTo(ScheduleSeatStatus.AVAILABLE);
    }

    @Test void ownershipPaginationAndDeparturePolicyAreEnforced() {
        var h = hold(fixture.first, fixture.a);
        assertThat(queries.list(fixture.second.getId(), null, 0, 10).content()).isEmpty();
        assertThat(queries.list(fixture.first.getId(), ReservationStatus.PENDING_PAYMENT, 0, 1).totalElements()).isEqualTo(1);
        assertThatThrownBy(() -> queries.getMine(fixture.second, h.reservationId())).isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> payments.cancelReservation(fixture.second, h.reservationId())).isInstanceOf(BusinessException.class);
        payments.simulateSuccess(fixture.first, h.paymentId());
        clock.set(START.plusSeconds(86400));
        assertThatThrownBy(() -> payments.cancelReservation(fixture.first, h.reservationId()))
            .isInstanceOf(BusinessException.class).extracting("errorCode").isEqualTo(ErrorCode.DEPARTURE_PASSED);
        assertThatThrownBy(() -> hold(fixture.first, fixture.b)).isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> queries.list(null, null, -1, 10)).isInstanceOf(BusinessException.class);
    }

    @Test void partialHoldRollsBackAndAdminCannotBlockHeldSeats() {
        seatService.block(fixture.b);
        assertThatThrownBy(() -> hold(fixture.first, fixture.a, fixture.b))
            .isInstanceOf(BusinessException.class).extracting("errorCode").isEqualTo(ErrorCode.SEAT_BLOCKED);
        assertThat(seatState(fixture.a)).isEqualTo(ScheduleSeatStatus.AVAILABLE);
        assertThat(queries.list(fixture.first.getId(), null, 0, 10).content()).isEmpty();
        seatService.unblock(fixture.b);
        hold(fixture.first, fixture.a);
        assertThatThrownBy(() -> seatService.block(fixture.a)).isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> seatService.unblock(fixture.a)).isInstanceOf(BusinessException.class);
        assertThat(seatState(fixture.a)).isEqualTo(ScheduleSeatStatus.HELD);
    }

    @Test void httpSignupLoginHoldPaymentRefundAndAuthorization() throws Exception {
        var mapper = JsonMapper.builder().build();
        String email = UUID.randomUUID() + "@http.test";
        String credentials = "{\"email\":\"" + email + "\",\"password\":\"TestPass123!\",\"name\":\"HTTP User\"}";
        assertThat(request("POST", "/api/auth/signup", credentials, null).statusCode()).isEqualTo(200);
        var login = request("POST", "/api/auth/login", credentials, null);
        String token = mapper.readTree(login.body()).path("data").path("accessToken").asText();
        var held = request("POST", "/api/reservations/hold", "{\"scheduleId\":" + fixture.schedule + ",\"scheduleSeatIds\":[" + fixture.a + "]}", token);
        assertThat(held.statusCode()).isEqualTo(200);
        var data = mapper.readTree(held.body()).path("data");
        long rid = data.path("reservationId").asLong(), pid = data.path("paymentId").asLong();
        assertThat(request("GET", "/api/reservations", null, token).statusCode()).isEqualTo(200);
        assertThat(request("GET", "/api/admin/reservations", null, token).statusCode()).isEqualTo(403);
        assertThat(request("GET", "/api/reservations?size=0", null, token).statusCode()).isEqualTo(400);
        assertThat(request("GET", "/api/reservations?status=UNKNOWN", null, token).statusCode()).isEqualTo(400);
        assertThat(request("POST", "/api/payments/" + pid + "/simulate-success", "{}", token).statusCode()).isEqualTo(200);
        var refund = request("POST", "/api/reservations/" + rid + "/cancel", "{}", token);
        assertThat(refund.statusCode()).isEqualTo(200);
        assertThat(mapper.readTree(refund.body()).path("data").path("payment").path("status").asText()).isEqualTo("REFUNDED");
        new TransactionTemplate(transactions).executeWithoutResult(status -> em.createNativeQuery("update users set role = 'ADMIN' where email = :email").setParameter("email", email).executeUpdate());
        assertThat(request("GET", "/api/admin/reservations", null, token).statusCode()).isEqualTo(200);
        assertThat(request("GET", "/api/admin/reservations/" + rid, null, token).statusCode()).isEqualTo(200);
    }

    private HttpResponse<String> request(String method, String path, String body, String token) throws Exception {
        var b = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path)).timeout(Duration.ofSeconds(15)).header("Content-Type", "application/json");
        if (token != null) b.header("Authorization", "Bearer " + token);
        return HttpClient.newHttpClient().send(b.method(method, body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body)).build(), HttpResponse.BodyHandlers.ofString());
    }
    private ReservationHoldResponse hold(UserPrincipal principal, Long... ids) { return reservations.hold(principal, new ReservationHoldRequest(fixture.schedule, List.of(ids))); }
    private ScheduleSeatStatus seatState(Long id) { return new TransactionTemplate(transactions).execute(s -> em.find(ScheduleSeat.class, id).getStatus()); }
    private List<Object> race(Callable<?> first, Callable<?> second) throws Exception {
        var ready = new CountDownLatch(2); var start = new CountDownLatch(1); var executor = Executors.newFixedThreadPool(2);
        try {
            var futures = List.of(first, second).stream().map(task -> executor.submit(() -> {
                ready.countDown();
                if (!start.await(10, TimeUnit.SECONDS)) throw new IllegalStateException("Start timed out");
                try { return task.call(); } catch (BusinessException e) { return e.getErrorCode(); }
            })).toList();
            assertThat(ready.await(10, TimeUnit.SECONDS)).isTrue(); start.countDown();
            List<Object> results = new ArrayList<>();
            for (var future : futures) results.add(future.get(15, TimeUnit.SECONDS));
            return results;
        } finally { executor.shutdownNow(); }
    }
    private record Fixture(UserPrincipal first, UserPrincipal second, Long schedule, Long a, Long b) {}
    @TestConfiguration static class TimeConfiguration { @Bean @Primary MutableClock testClock() { return new MutableClock(); } }
    static class MutableClock extends Clock {
        private final AtomicReference<Instant> instant = new AtomicReference<>(START);
        void set(Instant value) { instant.set(value); }
        @Override public ZoneId getZone() { return ZoneId.of("Asia/Seoul"); }
        @Override public Clock withZone(ZoneId zone) { return Clock.fixed(instant(), zone); }
        @Override public Instant instant() { return instant.get(); }
    }
}
