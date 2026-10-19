package com.railops.reservation.service;

import com.railops.auth.security.UserPrincipal;
import com.railops.common.error.BusinessException;
import com.railops.common.error.ErrorCode;
import com.railops.common.response.PageResponse;
import com.railops.payment.domain.PaymentStatus;
import com.railops.payment.repository.PaymentRepository;
import com.railops.reservation.domain.ReservationStatus;
import com.railops.reservation.dto.ReservationDetailResponse;
import com.railops.reservation.dto.ReservationSummaryResponse;
import com.railops.reservation.repository.ReservationRepository;
import com.railops.reservation.repository.ReservationSeatRepository;
import com.railops.schedule.domain.TrainScheduleStatus;
import java.time.Clock;
import java.time.LocalDateTime;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ReservationQueryService {
    private final ReservationRepository reservations;
    private final ReservationSeatRepository seats;
    private final PaymentRepository payments;
    private final Clock clock;

    public ReservationQueryService(ReservationRepository reservations, ReservationSeatRepository seats,
        PaymentRepository payments, Clock clock) {
        this.reservations = reservations;
        this.seats = seats;
        this.payments = payments;
        this.clock = clock;
    }

    public PageResponse<ReservationSummaryResponse> list(Long userId, ReservationStatus status, int page, int size) {
        if (page < 0 || size < 1 || size > 100) throw new BusinessException(ErrorCode.INVALID_REQUEST);
        var paging = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt", "id"));
        return PageResponse.from(reservations.findPage(userId, status, paging).map(ReservationSummaryResponse::from));
    }

    public ReservationDetailResponse getMine(UserPrincipal principal, Long id) { return detail(principal.getId(), id); }

    public ReservationDetailResponse detail(Long userId, Long id) {
        var r = reservations.findById(id).orElseThrow(() -> new BusinessException(ErrorCode.RESERVATION_NOT_FOUND));
        if (userId != null && !r.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.RESERVATION_NOT_OWNED);
        }
        var p = payments.findByReservation_Id(id).orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));
        var now = LocalDateTime.now(clock);
        boolean pending = r.getStatus() == ReservationStatus.PENDING_PAYMENT && p.getStatus() == PaymentStatus.READY
            && r.getHoldExpiresAt().isAfter(now);
        boolean beforeDeparture = r.getTrainSchedule().getDepartureTime().isAfter(now);
        var scheduleStatus = r.getTrainSchedule().getStatus();
        boolean bookable = scheduleStatus == TrainScheduleStatus.SCHEDULED || scheduleStatus == TrainScheduleStatus.DELAYED;
        return new ReservationDetailResponse(ReservationSummaryResponse.from(r),
            seats.findByReservationIdForPayment(id).stream().map(ReservationDetailResponse.SeatItem::from).toList(),
            ReservationDetailResponse.PaymentItem.from(p),
            pending || (r.getStatus() == ReservationStatus.CONFIRMED && p.getStatus() == PaymentStatus.SUCCESS && beforeDeparture),
            pending && beforeDeparture && bookable);
    }
}
