package com.railops.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.railops.auth.security.UserPrincipal;
import com.railops.common.error.BusinessException;
import com.railops.common.error.ErrorCode;
import com.railops.payment.domain.Payment;
import com.railops.payment.domain.PaymentStatus;
import com.railops.payment.repository.PaymentRepository;
import com.railops.reservation.domain.Reservation;
import com.railops.reservation.domain.ReservationSeat;
import com.railops.reservation.domain.ReservationStatus;
import com.railops.reservation.dto.ReservationHoldRequest;
import com.railops.reservation.dto.ReservationHoldResponse;
import com.railops.reservation.repository.ReservationRepository;
import com.railops.reservation.repository.ReservationSeatRepository;
import com.railops.route.domain.Route;
import com.railops.schedule.domain.TrainSchedule;
import com.railops.schedule.repository.TrainScheduleRepository;
import com.railops.seat.domain.ScheduleSeat;
import com.railops.seat.domain.ScheduleSeatStatus;
import com.railops.seat.repository.ScheduleSeatRepository;
import com.railops.station.domain.Station;
import com.railops.train.domain.Car;
import com.railops.train.domain.Seat;
import com.railops.train.domain.SeatType;
import com.railops.train.domain.Train;
import com.railops.user.domain.User;
import com.railops.user.repository.UserRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(
        Instant.parse("2026-08-01T00:00:00Z"),
        ZoneId.of("UTC")
    );

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ReservationSeatRepository reservationSeatRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private TrainScheduleRepository trainScheduleRepository;

    @Mock
    private ScheduleSeatRepository scheduleSeatRepository;

    @Mock
    private UserRepository userRepository;

    private ReservationService reservationService;

    @BeforeEach
    void setUp() {
        reservationService = new ReservationService(
            reservationRepository,
            reservationSeatRepository,
            paymentRepository,
            trainScheduleRepository,
            scheduleSeatRepository,
            userRepository,
            FIXED_CLOCK
        );
    }

    @Test
    void holdCreatesPendingReservationPaymentAndHoldsSeats() {
        User user = userWithId(1L);
        UserPrincipal principal = UserPrincipal.from(user);
        Train train = trainWithId(10L);
        TrainSchedule schedule = scheduleWithId(100L, train);
        Car car = carWithId(20L, train, 1);
        ScheduleSeat seatA = scheduleSeatWithId(1000L, schedule, seatWithId(200L, car, "12A", SeatType.WINDOW));
        ScheduleSeat seatB = scheduleSeatWithId(1001L, schedule, seatWithId(201L, car, "12B", SeatType.AISLE));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(trainScheduleRepository.findById(100L)).thenReturn(Optional.of(schedule));
        when(scheduleSeatRepository.findAllByIdInForUpdate(List.of(1000L, 1001L))).thenReturn(List.of(seatA, seatB));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> {
            Reservation reservation = invocation.getArgument(0);
            ReflectionTestUtils.setField(reservation, "id", 500L);
            return reservation;
        });
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            ReflectionTestUtils.setField(payment, "id", 600L);
            return payment;
        });

        ReservationHoldResponse response = reservationService.hold(
            principal,
            new ReservationHoldRequest(100L, List.of(1000L, 1001L))
        );

        assertThat(response.reservationId()).isEqualTo(500L);
        assertThat(response.paymentId()).isEqualTo(600L);
        assertThat(response.status()).isEqualTo(ReservationStatus.PENDING_PAYMENT);
        assertThat(response.scheduleId()).isEqualTo(100L);
        assertThat(response.scheduleSeatIds()).containsExactly(1000L, 1001L);
        assertThat(response.holdExpiresAt()).isEqualTo(LocalDateTime.of(2026, 8, 1, 0, 10));
        assertThat(seatA.getStatus()).isEqualTo(ScheduleSeatStatus.HELD);
        assertThat(seatA.getHeldByUser()).isEqualTo(user);
        assertThat(seatB.getStatus()).isEqualTo(ScheduleSeatStatus.HELD);

        ArgumentCaptor<Iterable<ReservationSeat>> captor = ArgumentCaptor.forClass(Iterable.class);
        verify(reservationSeatRepository).saveAll(captor.capture());
        List<ReservationSeat> savedReservationSeats = new ArrayList<>();
        captor.getValue().forEach(savedReservationSeats::add);
        assertThat(savedReservationSeats).hasSize(2);
    }

    @Test
    void holdRejectsBlockedSeat() {
        User user = userWithId(1L);
        Train train = trainWithId(10L);
        TrainSchedule schedule = scheduleWithId(100L, train);
        ScheduleSeat scheduleSeat = scheduleSeatWithId(
            1000L,
            schedule,
            seatWithId(200L, carWithId(20L, train, 1), "12A", SeatType.WINDOW)
        );
        scheduleSeat.block();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(trainScheduleRepository.findById(100L)).thenReturn(Optional.of(schedule));
        when(scheduleSeatRepository.findAllByIdInForUpdate(List.of(1000L))).thenReturn(List.of(scheduleSeat));

        assertThatThrownBy(() -> reservationService.hold(
            UserPrincipal.from(user),
            new ReservationHoldRequest(100L, List.of(1000L))
        ))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.SEAT_BLOCKED);
    }

    @Test
    void holdRejectsSeatFromOtherSchedule() {
        User user = userWithId(1L);
        Train train = trainWithId(10L);
        TrainSchedule requestedSchedule = scheduleWithId(100L, train);
        TrainSchedule otherSchedule = scheduleWithId(101L, train);
        ScheduleSeat scheduleSeat = scheduleSeatWithId(
            1000L,
            otherSchedule,
            seatWithId(200L, carWithId(20L, train, 1), "12A", SeatType.WINDOW)
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(trainScheduleRepository.findById(100L)).thenReturn(Optional.of(requestedSchedule));
        when(scheduleSeatRepository.findAllByIdInForUpdate(List.of(1000L))).thenReturn(List.of(scheduleSeat));

        assertThatThrownBy(() -> reservationService.hold(
            UserPrincipal.from(user),
            new ReservationHoldRequest(100L, List.of(1000L))
        ))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.SCHEDULE_SEAT_NOT_FOUND);
    }

    @Test
    void expireHoldsExpiresPaymentsReservationsAndReleasesSeats() {
        when(paymentRepository.expireReadyPayments(
            PaymentStatus.READY,
            PaymentStatus.EXPIRED,
            ReservationStatus.PENDING_PAYMENT,
            LocalDateTime.of(2026, 8, 1, 0, 0)
        )).thenReturn(1);
        when(reservationRepository.expirePendingReservations(
            ReservationStatus.PENDING_PAYMENT,
            ReservationStatus.EXPIRED,
            LocalDateTime.of(2026, 8, 1, 0, 0)
        )).thenReturn(2);
        when(scheduleSeatRepository.releaseExpiredHolds(LocalDateTime.of(2026, 8, 1, 0, 0))).thenReturn(3);

        int affectedRows = reservationService.expireHolds();

        assertThat(affectedRows).isEqualTo(6);
    }

    private User userWithId(Long id) {
        User user = User.createUser("user@example.com", "password", "User");
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private Train trainWithId(Long id) {
        Train train = Train.create("KTX-101", "KTX", "KTX 101");
        ReflectionTestUtils.setField(train, "id", id);
        return train;
    }

    private Car carWithId(Long id, Train train, Integer carNo) {
        Car car = Car.create(train, carNo, 56);
        ReflectionTestUtils.setField(car, "id", id);
        return car;
    }

    private Seat seatWithId(Long id, Car car, String seatNo, SeatType seatType) {
        Seat seat = Seat.create(car, seatNo, seatType);
        ReflectionTestUtils.setField(seat, "id", id);
        return seat;
    }

    private TrainSchedule scheduleWithId(Long id, Train train) {
        Station origin = stationWithId(200L, "Seoul", "SEOUL", "Seoul");
        Station destination = stationWithId(201L, "Busan", "BUSAN", "Busan");
        Route route = Route.create("Seoul-Busan", origin, destination);
        ReflectionTestUtils.setField(route, "id", 300L);
        TrainSchedule schedule = TrainSchedule.create(
            train,
            route,
            LocalDate.of(2026, 8, 1),
            LocalDateTime.of(2026, 8, 1, 9, 0),
            LocalDateTime.of(2026, 8, 1, 11, 40)
        );
        ReflectionTestUtils.setField(schedule, "id", id);
        return schedule;
    }

    private Station stationWithId(Long id, String name, String code, String city) {
        Station station = Station.create(name, code, city);
        ReflectionTestUtils.setField(station, "id", id);
        return station;
    }

    private ScheduleSeat scheduleSeatWithId(Long id, TrainSchedule schedule, Seat seat) {
        ScheduleSeat scheduleSeat = ScheduleSeat.createAvailable(schedule, seat);
        ReflectionTestUtils.setField(scheduleSeat, "id", id);
        return scheduleSeat;
    }
}