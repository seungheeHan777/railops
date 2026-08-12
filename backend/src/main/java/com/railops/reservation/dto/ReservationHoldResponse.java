package com.railops.reservation.dto;

import com.railops.reservation.domain.ReservationStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ReservationHoldResponse(
    Long reservationId,
    String reservationNo,
    Long paymentId,
    String paymentNo,
    Long scheduleId,
    List<Long> scheduleSeatIds,
    ReservationStatus status,
    BigDecimal amount,
    LocalDateTime holdExpiresAt
) {
}