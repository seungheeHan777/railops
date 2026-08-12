package com.railops.reservation.service;

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
import com.railops.schedule.domain.TrainSchedule;
import com.railops.schedule.domain.TrainScheduleStatus;
import com.railops.schedule.repository.TrainScheduleRepository;
import com.railops.seat.domain.ScheduleSeat;
import com.railops.seat.domain.ScheduleSeatStatus;
import com.railops.seat.repository.ScheduleSeatRepository;
import com.railops.user.domain.User;
import com.railops.user.repository.UserRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashSet;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ReservationService {

    private static final long HOLD_MINUTES = 10;
    private static final BigDecimal TEMPORARY_PRICE = BigDecimal.ZERO;
    private static final DateTimeFormatter NUMBER_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final ReservationRepository reservationRepository;
    private final ReservationSeatRepository reservationSeatRepository;
    private final PaymentRepository paymentRepository;
    private final TrainScheduleRepository trainScheduleRepository;
    private final ScheduleSeatRepository scheduleSeatRepository;
    private final UserRepository userRepository;
    private final Clock clock;

    public ReservationService(
        ReservationRepository reservationRepository,
        ReservationSeatRepository reservationSeatRepository,
        PaymentRepository paymentRepository,
        TrainScheduleRepository trainScheduleRepository,
        ScheduleSeatRepository scheduleSeatRepository,
        UserRepository userRepository,
        Clock clock
    ) {
        this.reservationRepository = reservationRepository;
        this.reservationSeatRepository = reservationSeatRepository;
        this.paymentRepository = paymentRepository;
        this.trainScheduleRepository = trainScheduleRepository;
        this.scheduleSeatRepository = scheduleSeatRepository;
        this.userRepository = userRepository;
        this.clock = clock;
    }

    @Transactional
    public ReservationHoldResponse hold(UserPrincipal principal, ReservationHoldRequest request) {
        User user = userRepository.findById(principal.getId())
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        TrainSchedule schedule = trainScheduleRepository.findById(request.scheduleId())
            .orElseThrow(() -> new BusinessException(ErrorCode.SCHEDULE_NOT_FOUND));
        validateBookable(schedule);

        List<Long> scheduleSeatIds = distinctIds(request.scheduleSeatIds());
        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime holdExpiresAt = now.plusMinutes(HOLD_MINUTES);
        List<ScheduleSeat> scheduleSeats = scheduleSeatRepository.findAllByIdInForUpdate(scheduleSeatIds);
        if (scheduleSeats.size() != scheduleSeatIds.size()) {
            throw new BusinessException(ErrorCode.SCHEDULE_SEAT_NOT_FOUND);
        }

        for (ScheduleSeat scheduleSeat : scheduleSeats) {
            validateSeat(scheduleSeat, schedule, now);
            scheduleSeat.hold(user, holdExpiresAt);
        }

        BigDecimal amount = TEMPORARY_PRICE.multiply(BigDecimal.valueOf(scheduleSeats.size()));
        Reservation reservation = reservationRepository.save(
            Reservation.createPendingPayment(generateReservationNo(user.getId(), now), user, schedule, amount, holdExpiresAt)
        );
        reservationSeatRepository.saveAll(scheduleSeats.stream()
            .map(scheduleSeat -> ReservationSeat.create(reservation, scheduleSeat, TEMPORARY_PRICE))
            .toList());
        Payment payment = paymentRepository.save(
            Payment.createReady(reservation, generatePaymentNo(user.getId(), now), amount, now)
        );

        return new ReservationHoldResponse(
            reservation.getId(),
            reservation.getReservationNo(),
            payment.getId(),
            payment.getPaymentNo(),
            schedule.getId(),
            scheduleSeats.stream().map(ScheduleSeat::getId).toList(),
            reservation.getStatus(),
            amount,
            holdExpiresAt
        );
    }

    @Transactional
    public int expireHolds() {
        LocalDateTime now = LocalDateTime.now(clock);
        int expiredPayments = paymentRepository.expireReadyPayments(
            PaymentStatus.READY,
            PaymentStatus.EXPIRED,
            ReservationStatus.PENDING_PAYMENT,
            now
        );
        int expiredReservations = reservationRepository.expirePendingReservations(
            ReservationStatus.PENDING_PAYMENT,
            ReservationStatus.EXPIRED,
            now
        );
        int releasedSeats = scheduleSeatRepository.releaseExpiredHolds(now);
        return expiredPayments + expiredReservations + releasedSeats;
    }

    private void validateBookable(TrainSchedule schedule) {
        if (schedule.getStatus() != TrainScheduleStatus.SCHEDULED && schedule.getStatus() != TrainScheduleStatus.DELAYED) {
            throw new BusinessException(ErrorCode.SCHEDULE_NOT_BOOKABLE);
        }
    }

    private void validateSeat(ScheduleSeat scheduleSeat, TrainSchedule schedule, LocalDateTime now) {
        if (!scheduleSeat.getTrainSchedule().getId().equals(schedule.getId())) {
            throw new BusinessException(ErrorCode.SCHEDULE_SEAT_NOT_FOUND);
        }
        if (scheduleSeat.isExpiredHold(now)) {
            scheduleSeat.releaseHold();
        }
        if (scheduleSeat.getStatus() == ScheduleSeatStatus.BLOCKED) {
            throw new BusinessException(ErrorCode.SEAT_BLOCKED);
        }
        if (scheduleSeat.getStatus() == ScheduleSeatStatus.HELD) {
            throw new BusinessException(ErrorCode.SEAT_ALREADY_HELD);
        }
        if (scheduleSeat.getStatus() == ScheduleSeatStatus.RESERVED) {
            throw new BusinessException(ErrorCode.SEAT_ALREADY_RESERVED);
        }
        if (scheduleSeat.getStatus() != ScheduleSeatStatus.AVAILABLE) {
            throw new BusinessException(ErrorCode.SEAT_NOT_AVAILABLE);
        }
    }

    private List<Long> distinctIds(List<Long> ids) {
        return new LinkedHashSet<>(ids).stream().toList();
    }

    private String generateReservationNo(Long userId, LocalDateTime now) {
        return "R" + now.format(NUMBER_TIME_FORMAT) + "U" + userId;
    }

    private String generatePaymentNo(Long userId, LocalDateTime now) {
        return "P" + now.format(NUMBER_TIME_FORMAT) + "U" + userId;
    }
}