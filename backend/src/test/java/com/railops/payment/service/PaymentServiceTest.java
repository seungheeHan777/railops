package com.railops.payment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.railops.auth.security.UserPrincipal;
import com.railops.common.error.BusinessException;
import com.railops.common.error.ErrorCode;
import com.railops.payment.domain.Payment;
import com.railops.payment.domain.PaymentStatus;
import com.railops.payment.dto.PaymentResultResponse;
import com.railops.payment.repository.PaymentRepository;
import com.railops.reservation.domain.Reservation;
import com.railops.reservation.domain.ReservationSeat;
import com.railops.reservation.domain.ReservationStatus;
import com.railops.reservation.repository.ReservationSeatRepository;
import com.railops.route.domain.Route;
import com.railops.schedule.domain.TrainSchedule;
import com.railops.seat.domain.ScheduleSeat;
import com.railops.seat.domain.ScheduleSeatStatus;
import com.railops.station.domain.Station;
import com.railops.train.domain.Car;
import com.railops.train.domain.Seat;
import com.railops.train.domain.SeatType;
import com.railops.train.domain.Train;
import com.railops.user.domain.User;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(
        Instant.parse("2026-08-01T00:00:00Z"),
        ZoneId.of("UTC")
    );

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private ReservationSeatRepository reservationSeatRepository;

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService(paymentRepository, reservationSeatRepository, FIXED_CLOCK);
    }

    @Test
    void simulateSuccessConfirmsReservationAndReservesSeat() {
        Fixtures fixtures = fixtures(LocalDateTime.of(2026, 8, 1, 0, 10));
        when(paymentRepository.findByIdForUpdate(600L)).thenReturn(Optional.of(fixtures.payment));
        when(reservationSeatRepository.findByReservationIdForPayment(500L)).thenReturn(List.of(fixtures.reservationSeat));

        PaymentResultResponse response = paymentService.simulateSuccess(UserPrincipal.from(fixtures.user), 600L);

        assertThat(response.paymentStatus()).isEqualTo(PaymentStatus.SUCCESS);
        assertThat(response.reservationStatus()).isEqualTo(ReservationStatus.CONFIRMED);
        assertThat(fixtures.scheduleSeat.getStatus()).isEqualTo(ScheduleSeatStatus.RESERVED);
        assertThat(fixtures.scheduleSeat.getHeldByUser()).isNull();
    }

    @Test
    void simulateFailMarksReservationFailedAndReleasesSeat() {
        Fixtures fixtures = fixtures(LocalDateTime.of(2026, 8, 1, 0, 10));
        when(paymentRepository.findByIdForUpdate(600L)).thenReturn(Optional.of(fixtures.payment));
        when(reservationSeatRepository.findByReservationIdForPayment(500L)).thenReturn(List.of(fixtures.reservationSeat));

        PaymentResultResponse response = paymentService.simulateFail(UserPrincipal.from(fixtures.user), 600L);

        assertThat(response.paymentStatus()).isEqualTo(PaymentStatus.FAILED);
        assertThat(response.reservationStatus()).isEqualTo(ReservationStatus.PAYMENT_FAILED);
        assertThat(fixtures.scheduleSeat.getStatus()).isEqualTo(ScheduleSeatStatus.AVAILABLE);
    }

    @Test
    void simulateSuccessExpiresWhenHoldIsExpired() {
        Fixtures fixtures = fixtures(LocalDateTime.of(2026, 7, 31, 23, 59));
        when(paymentRepository.findByIdForUpdate(600L)).thenReturn(Optional.of(fixtures.payment));
        when(reservationSeatRepository.findByReservationIdForPayment(500L)).thenReturn(List.of(fixtures.reservationSeat));

        assertThatThrownBy(() -> paymentService.simulateSuccess(UserPrincipal.from(fixtures.user), 600L))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.HOLD_EXPIRED);
        assertThat(fixtures.payment.getStatus()).isEqualTo(PaymentStatus.EXPIRED);
        assertThat(fixtures.reservation.getStatus()).isEqualTo(ReservationStatus.EXPIRED);
        assertThat(fixtures.scheduleSeat.getStatus()).isEqualTo(ScheduleSeatStatus.AVAILABLE);
    }

    @Test
    void simulateSuccessRejectsOtherUserPayment() {
        Fixtures fixtures = fixtures(LocalDateTime.of(2026, 8, 1, 0, 10));
        User otherUser = userWithId(2L);
        when(paymentRepository.findByIdForUpdate(600L)).thenReturn(Optional.of(fixtures.payment));

        assertThatThrownBy(() -> paymentService.simulateSuccess(UserPrincipal.from(otherUser), 600L))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.RESERVATION_NOT_OWNED);
    }

    private Fixtures fixtures(LocalDateTime holdExpiresAt) {
        User user = userWithId(1L);
        Train train = trainWithId(10L);
        TrainSchedule schedule = scheduleWithId(100L, train);
        Reservation reservation = Reservation.createPendingPayment(
            "R20260801000000U1",
            user,
            schedule,
            BigDecimal.ZERO,
            holdExpiresAt
        );
        ReflectionTestUtils.setField(reservation, "id", 500L);
        Payment payment = Payment.createReady(reservation, "P20260801000000U1", BigDecimal.ZERO, LocalDateTime.of(2026, 8, 1, 0, 0));
        ReflectionTestUtils.setField(payment, "id", 600L);
        Car car = carWithId(20L, train, 1);
        ScheduleSeat scheduleSeat = ScheduleSeat.createAvailable(schedule, seatWithId(200L, car, "12A", SeatType.WINDOW));
        ReflectionTestUtils.setField(scheduleSeat, "id", 1000L);
        scheduleSeat.hold(user, holdExpiresAt);
        ReservationSeat reservationSeat = ReservationSeat.create(reservation, scheduleSeat, BigDecimal.ZERO);
        return new Fixtures(user, reservation, payment, scheduleSeat, reservationSeat);
    }

    private User userWithId(Long id) {
        User user = User.createUser("user" + id + "@example.com", "password", "User" + id);
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

    private record Fixtures(
        User user,
        Reservation reservation,
        Payment payment,
        ScheduleSeat scheduleSeat,
        ReservationSeat reservationSeat
    ) {
    }
}