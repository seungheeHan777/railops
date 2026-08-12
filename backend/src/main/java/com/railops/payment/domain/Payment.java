package com.railops.payment.domain;

import com.railops.reservation.domain.Reservation;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reservation_id", nullable = false, unique = true)
    private Reservation reservation;

    @Column(nullable = false, unique = true, length = 50)
    private String paymentNo;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Column(nullable = false)
    private LocalDateTime requestedAt;

    private LocalDateTime approvedAt;
    private LocalDateTime failedAt;
    private LocalDateTime canceledAt;
    private LocalDateTime expiredAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    protected Payment() {
    }

    private Payment(Reservation reservation, String paymentNo, BigDecimal amount, LocalDateTime requestedAt) {
        this.reservation = reservation;
        this.paymentNo = paymentNo;
        this.amount = amount;
        this.status = PaymentStatus.READY;
        this.requestedAt = requestedAt;
        this.createdAt = requestedAt;
        this.updatedAt = requestedAt;
    }

    public static Payment createReady(Reservation reservation, String paymentNo, BigDecimal amount, LocalDateTime requestedAt) {
        return new Payment(reservation, paymentNo, amount, requestedAt);
    }

    public void succeed(LocalDateTime now) {
        this.status = PaymentStatus.SUCCESS;
        this.approvedAt = now;
        this.updatedAt = now;
    }

    public void fail(LocalDateTime now) {
        this.status = PaymentStatus.FAILED;
        this.failedAt = now;
        this.updatedAt = now;
    }

    public void cancel(LocalDateTime now) {
        this.status = PaymentStatus.CANCELED;
        this.canceledAt = now;
        this.updatedAt = now;
    }

    public void expire(LocalDateTime now) {
        this.status = PaymentStatus.EXPIRED;
        this.expiredAt = now;
        this.updatedAt = now;
    }

    public Long getId() {
        return id;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public String getPaymentNo() {
        return paymentNo;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }

    public LocalDateTime getApprovedAt() {
        return approvedAt;
    }

    public LocalDateTime getFailedAt() {
        return failedAt;
    }

    public LocalDateTime getCanceledAt() {
        return canceledAt;
    }

    public LocalDateTime getExpiredAt() {
        return expiredAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}