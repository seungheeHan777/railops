package com.railops.reservation.dto;

import com.railops.reservation.domain.Reservation;
import com.railops.reservation.domain.ReservationStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ReservationSummaryResponse(Long id, String reservationNo, Long userId, String userName,
    Long scheduleId, String trainNo, String origin, String destination, LocalDateTime departureTime,
    LocalDateTime arrivalTime, ReservationStatus status, BigDecimal totalAmount,
    LocalDateTime holdExpiresAt, LocalDateTime createdAt) {
    public static ReservationSummaryResponse from(Reservation r) {
        var s = r.getTrainSchedule();
        return new ReservationSummaryResponse(r.getId(), r.getReservationNo(), r.getUser().getId(),
            r.getUser().getName(), s.getId(), s.getTrain().getTrainNo(),
            s.getRoute().getOriginStation().getName(), s.getRoute().getDestinationStation().getName(),
            s.getDepartureTime(), s.getArrivalTime(), r.getStatus(), r.getTotalAmount(),
            r.getHoldExpiresAt(), r.getCreatedAt());
    }
}
