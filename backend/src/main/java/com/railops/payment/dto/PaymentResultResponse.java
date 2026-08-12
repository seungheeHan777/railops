package com.railops.payment.dto;

import com.railops.payment.domain.Payment;
import com.railops.payment.domain.PaymentStatus;
import com.railops.reservation.domain.ReservationStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResultResponse(
    Long paymentId,
    String paymentNo,
    Long reservationId,
    PaymentStatus paymentStatus,
    ReservationStatus reservationStatus,
    BigDecimal amount,
    LocalDateTime processedAt
) {

    public static PaymentResultResponse from(Payment payment, LocalDateTime processedAt) {
        return new PaymentResultResponse(
            payment.getId(),
            payment.getPaymentNo(),
            payment.getReservation().getId(),
            payment.getStatus(),
            payment.getReservation().getStatus(),
            payment.getAmount(),
            processedAt
        );
    }
}