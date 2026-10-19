package com.railops.reservation.dto;

import com.railops.payment.domain.Payment;
import com.railops.payment.domain.PaymentStatus;
import com.railops.reservation.domain.ReservationSeat;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ReservationDetailResponse(ReservationSummaryResponse reservation, List<SeatItem> seats,
    PaymentItem payment, boolean canCancel, boolean canPay) {
    public record SeatItem(Long scheduleSeatId, int carNo, String seatNo, BigDecimal price) {
        public static SeatItem from(ReservationSeat rs) {
            var seat = rs.getScheduleSeat().getSeat();
            return new SeatItem(rs.getScheduleSeat().getId(), seat.getCar().getCarNo(), seat.getSeatNo(), rs.getPrice());
        }
    }

    public record PaymentItem(Long id, String paymentNo, PaymentStatus status, BigDecimal amount,
        BigDecimal refundedAmount, LocalDateTime refundedAt) {
        public static PaymentItem from(Payment p) {
            return new PaymentItem(p.getId(), p.getPaymentNo(), p.getStatus(), p.getAmount(),
                p.getRefundedAmount(), p.getRefundedAt());
        }
    }
}
