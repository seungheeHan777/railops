package com.railops.payment.service;

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
import com.railops.seat.domain.ScheduleSeat;
import com.railops.seat.domain.ScheduleSeatStatus;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ReservationSeatRepository reservationSeatRepository;
    private final Clock clock;

    public PaymentService(
        PaymentRepository paymentRepository,
        ReservationSeatRepository reservationSeatRepository,
        Clock clock
    ) {
        this.paymentRepository = paymentRepository;
        this.reservationSeatRepository = reservationSeatRepository;
        this.clock = clock;
    }

    @Transactional
    public PaymentResultResponse simulateSuccess(UserPrincipal principal, Long paymentId) {
        LocalDateTime now = LocalDateTime.now(clock);
        Payment payment = getReadyPayment(principal, paymentId);
        Reservation reservation = payment.getReservation();
        List<ReservationSeat> reservationSeats = reservationSeatRepository.findByReservationIdForPayment(reservation.getId());
        validateHoldNotExpired(payment, reservation, reservationSeats, principal.getId(), now);

        payment.succeed(now);
        reservation.confirm(now);
        reservationSeats.forEach(reservationSeat -> reservationSeat.getScheduleSeat().reserve());
        return PaymentResultResponse.from(payment, now);
    }

    @Transactional
    public PaymentResultResponse simulateFail(UserPrincipal principal, Long paymentId) {
        LocalDateTime now = LocalDateTime.now(clock);
        Payment payment = getReadyPayment(principal, paymentId);
        Reservation reservation = payment.getReservation();
        List<ReservationSeat> reservationSeats = reservationSeatRepository.findByReservationIdForPayment(reservation.getId());

        payment.fail(now);
        reservation.failPayment(now);
        reservationSeats.forEach(reservationSeat -> reservationSeat.getScheduleSeat().releaseHold());
        return PaymentResultResponse.from(payment, now);
    }

    @Transactional
    public PaymentResultResponse simulateCancel(UserPrincipal principal, Long paymentId) {
        LocalDateTime now = LocalDateTime.now(clock);
        Payment payment = getReadyPayment(principal, paymentId);
        Reservation reservation = payment.getReservation();
        List<ReservationSeat> reservationSeats = reservationSeatRepository.findByReservationIdForPayment(reservation.getId());

        payment.cancel(now);
        reservation.cancel(now);
        reservationSeats.forEach(reservationSeat -> reservationSeat.getScheduleSeat().releaseHold());
        return PaymentResultResponse.from(payment, now);
    }

    private Payment getReadyPayment(UserPrincipal principal, Long paymentId) {
        Payment payment = paymentRepository.findByIdForUpdate(paymentId)
            .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));
        Reservation reservation = payment.getReservation();
        if (!reservation.getUser().getId().equals(principal.getId())) {
            throw new BusinessException(ErrorCode.RESERVATION_NOT_OWNED);
        }
        if (payment.getStatus() != PaymentStatus.READY) {
            throw new BusinessException(ErrorCode.PAYMENT_NOT_READY);
        }
        if (reservation.getStatus() != ReservationStatus.PENDING_PAYMENT) {
            throw new BusinessException(ErrorCode.RESERVATION_NOT_PENDING_PAYMENT);
        }
        return payment;
    }

    private void validateHoldNotExpired(
        Payment payment,
        Reservation reservation,
        List<ReservationSeat> reservationSeats,
        Long userId,
        LocalDateTime now
    ) {
        if (!reservation.getHoldExpiresAt().isAfter(now)) {
            expirePayment(payment, reservation, reservationSeats, now);
            throw new BusinessException(ErrorCode.HOLD_EXPIRED);
        }
        for (ReservationSeat reservationSeat : reservationSeats) {
            ScheduleSeat scheduleSeat = reservationSeat.getScheduleSeat();
            if (scheduleSeat.getStatus() != ScheduleSeatStatus.HELD) {
                throw new BusinessException(ErrorCode.SEAT_NOT_AVAILABLE);
            }
            if (scheduleSeat.getHeldByUser() == null || !scheduleSeat.getHeldByUser().getId().equals(userId)) {
                throw new BusinessException(ErrorCode.RESERVATION_NOT_OWNED);
            }
            if (scheduleSeat.getHoldExpiresAt() == null || !scheduleSeat.getHoldExpiresAt().isAfter(now)) {
                expirePayment(payment, reservation, reservationSeats, now);
                throw new BusinessException(ErrorCode.HOLD_EXPIRED);
            }
        }
    }

    private void expirePayment(
        Payment payment,
        Reservation reservation,
        List<ReservationSeat> reservationSeats,
        LocalDateTime now
    ) {
        payment.expire(now);
        reservation.expire(now);
        reservationSeats.forEach(reservationSeat -> reservationSeat.getScheduleSeat().releaseHold());
    }
}