package com.railops.payment.service;

import com.railops.auth.security.UserPrincipal;
import com.railops.common.error.BusinessException;
import com.railops.common.error.ErrorCode;
import com.railops.common.error.HoldExpiredException;
import com.railops.payment.domain.Payment;
import com.railops.payment.domain.PaymentStatus;
import com.railops.payment.dto.PaymentResultResponse;
import com.railops.payment.repository.PaymentRepository;
import com.railops.reservation.domain.Reservation;
import com.railops.reservation.domain.ReservationStatus;
import com.railops.reservation.repository.ReservationSeatRepository;
import com.railops.schedule.domain.TrainScheduleStatus;
import com.railops.seat.domain.ScheduleSeat;
import com.railops.seat.domain.ScheduleSeatStatus;
import com.railops.seat.repository.ScheduleSeatRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PaymentService {
    private final PaymentRepository payments;
    private final ReservationSeatRepository reservationSeats;
    private final ScheduleSeatRepository seats;
    private final Clock clock;

    public PaymentService(PaymentRepository payments, ReservationSeatRepository reservationSeats,
        ScheduleSeatRepository seats, Clock clock) {
        this.payments = payments;
        this.reservationSeats = reservationSeats;
        this.seats = seats;
        this.clock = clock;
    }

    @Transactional(noRollbackFor = HoldExpiredException.class)
    public PaymentResultResponse simulateSuccess(UserPrincipal principal, Long paymentId) {
        Payment p = ownedPayment(principal, paymentId);
        if (p.getStatus() == PaymentStatus.SUCCESS) return PaymentResultResponse.from(p, p.getApprovedAt());
        requireReady(p);
        List<ScheduleSeat> lockedSeats = lockSeats(p);
        LocalDateTime now = LocalDateTime.now(clock);
        requireLiveHold(p, lockedSeats, now);
        Reservation r = p.getReservation();
        if (!r.getTrainSchedule().getDepartureTime().isAfter(now)) throw new BusinessException(ErrorCode.DEPARTURE_PASSED);
        var status = r.getTrainSchedule().getStatus();
        if (status != TrainScheduleStatus.SCHEDULED && status != TrainScheduleStatus.DELAYED) {
            throw new BusinessException(ErrorCode.SCHEDULE_NOT_BOOKABLE);
        }
        for (ScheduleSeat seat : lockedSeats) {
            if (!belongsToHold(seat, r)) throw new BusinessException(ErrorCode.SEAT_NOT_AVAILABLE);
        }
        p.succeed(now);
        r.confirm(now);
        lockedSeats.forEach(ScheduleSeat::reserve);
        return PaymentResultResponse.from(p, now);
    }

    @Transactional(noRollbackFor = HoldExpiredException.class)
    public PaymentResultResponse simulateFail(UserPrincipal principal, Long paymentId) {
        Payment p = ownedPayment(principal, paymentId);
        if (p.getStatus() == PaymentStatus.FAILED) return PaymentResultResponse.from(p, p.getFailedAt());
        requireReady(p);
        List<ScheduleSeat> lockedSeats = lockSeats(p);
        LocalDateTime now = LocalDateTime.now(clock);
        requireLiveHold(p, lockedSeats, now);
        p.fail(now);
        p.getReservation().failPayment(now);
        releaseOwnedHolds(p.getReservation(), lockedSeats);
        return PaymentResultResponse.from(p, now);
    }

    @Transactional(noRollbackFor = HoldExpiredException.class)
    public PaymentResultResponse simulateCancel(UserPrincipal principal, Long paymentId) {
        Payment p = ownedPayment(principal, paymentId);
        if (p.getStatus() == PaymentStatus.CANCELED) return PaymentResultResponse.from(p, p.getCanceledAt());
        requireReady(p);
        List<ScheduleSeat> lockedSeats = lockSeats(p);
        LocalDateTime now = LocalDateTime.now(clock);
        requireLiveHold(p, lockedSeats, now);
        p.cancel(now);
        p.getReservation().cancel(now);
        releaseOwnedHolds(p.getReservation(), lockedSeats);
        return PaymentResultResponse.from(p, now);
    }

    @Transactional
    public PaymentResultResponse cancelReservation(UserPrincipal principal, Long reservationId) {
        Long paymentId = payments.findIdByReservationId(reservationId)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESERVATION_NOT_FOUND));
        Payment p = ownedPayment(principal, paymentId);
        Reservation r = p.getReservation();
        if (r.getStatus() == ReservationStatus.CANCELED &&
            (p.getStatus() == PaymentStatus.CANCELED || p.getStatus() == PaymentStatus.REFUNDED)) {
            return PaymentResultResponse.from(p, p.getUpdatedAt());
        }
        if (r.getStatus() != ReservationStatus.PENDING_PAYMENT && r.getStatus() != ReservationStatus.CONFIRMED) {
            throw new BusinessException(ErrorCode.RESERVATION_NOT_CANCELABLE);
        }
        List<ScheduleSeat> lockedSeats = lockSeats(p);
        LocalDateTime now = LocalDateTime.now(clock);
        if (r.getStatus() == ReservationStatus.PENDING_PAYMENT && p.getStatus() == PaymentStatus.READY) {
            if (!r.getHoldExpiresAt().isAfter(now)) {
                expire(p, lockedSeats, now);
            } else {
                p.cancel(now);
                r.cancel(now);
                releaseOwnedHolds(r, lockedSeats);
            }
        } else if (r.getStatus() == ReservationStatus.CONFIRMED && p.getStatus() == PaymentStatus.SUCCESS) {
            if (!r.getTrainSchedule().getDepartureTime().isAfter(now)) throw new BusinessException(ErrorCode.DEPARTURE_PASSED);
            if (lockedSeats.stream().anyMatch(s -> s.getStatus() != ScheduleSeatStatus.RESERVED)) {
                throw new BusinessException(ErrorCode.SEAT_NOT_AVAILABLE);
            }
            p.refund(now);
            r.cancel(now);
            lockedSeats.forEach(ScheduleSeat::releaseHold);
        } else {
            throw new BusinessException(ErrorCode.RESERVATION_NOT_CANCELABLE);
        }
        return PaymentResultResponse.from(p, now);
    }

    @Transactional
    public boolean expireIfDue(Long paymentId) {
        Payment p = payments.findByIdForUpdate(paymentId).orElse(null);
        if (p == null || p.getStatus() != PaymentStatus.READY ||
            p.getReservation().getStatus() != ReservationStatus.PENDING_PAYMENT) return false;
        List<ScheduleSeat> lockedSeats = lockSeats(p);
        LocalDateTime now = LocalDateTime.now(clock);
        if (p.getReservation().getHoldExpiresAt().isAfter(now)) return false;
        expire(p, lockedSeats, now);
        return true;
    }

    private Payment ownedPayment(UserPrincipal principal, Long paymentId) {
        Payment p = payments.findByIdForUpdate(paymentId)
            .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));
        if (!p.getReservation().getUser().getId().equals(principal.getId())) {
            throw new BusinessException(ErrorCode.RESERVATION_NOT_OWNED);
        }
        return p;
    }

    private void requireReady(Payment p) {
        if (p.getStatus() == PaymentStatus.EXPIRED) throw new HoldExpiredException();
        if (p.getStatus() != PaymentStatus.READY) throw new BusinessException(ErrorCode.PAYMENT_NOT_READY);
        if (p.getReservation().getStatus() != ReservationStatus.PENDING_PAYMENT) {
            throw new BusinessException(ErrorCode.RESERVATION_NOT_PENDING_PAYMENT);
        }
    }

    private List<ScheduleSeat> lockSeats(Payment p) {
        List<Long> ids = reservationSeats.findScheduleSeatIds(p.getReservation().getId());
        if (ids.isEmpty()) throw new BusinessException(ErrorCode.SCHEDULE_SEAT_NOT_FOUND);
        List<ScheduleSeat> locked = seats.findAllByIdInForUpdate(ids);
        if (locked.size() != ids.size()) throw new BusinessException(ErrorCode.SCHEDULE_SEAT_NOT_FOUND);
        return locked;
    }

    private void requireLiveHold(Payment p, List<ScheduleSeat> lockedSeats, LocalDateTime now) {
        if (!p.getReservation().getHoldExpiresAt().isAfter(now)) {
            expire(p, lockedSeats, now);
            throw new HoldExpiredException();
        }
    }

    private boolean belongsToHold(ScheduleSeat seat, Reservation r) {
        return seat.getStatus() == ScheduleSeatStatus.HELD && seat.getHeldByUser() != null
            && seat.getHeldByUser().getId().equals(r.getUser().getId())
            && r.getHoldExpiresAt().equals(seat.getHoldExpiresAt());
    }

    private void releaseOwnedHolds(Reservation r, List<ScheduleSeat> lockedSeats) {
        lockedSeats.stream().filter(s -> belongsToHold(s, r)).forEach(ScheduleSeat::releaseHold);
    }

    private void expire(Payment p, List<ScheduleSeat> lockedSeats, LocalDateTime now) {
        p.expire(now);
        p.getReservation().expire(now);
        releaseOwnedHolds(p.getReservation(), lockedSeats);
    }
}
