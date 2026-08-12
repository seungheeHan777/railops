package com.railops.reservation.domain;

import com.railops.schedule.domain.TrainSchedule;
import com.railops.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservations")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String reservationNo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "train_schedule_id", nullable = false)
    private TrainSchedule trainSchedule;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false)
    private LocalDateTime holdExpiresAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    protected Reservation() {
    }

    private Reservation(
        String reservationNo,
        User user,
        TrainSchedule trainSchedule,
        BigDecimal totalAmount,
        LocalDateTime holdExpiresAt
    ) {
        LocalDateTime now = LocalDateTime.now();
        this.reservationNo = reservationNo;
        this.user = user;
        this.trainSchedule = trainSchedule;
        this.status = ReservationStatus.PENDING_PAYMENT;
        this.totalAmount = totalAmount;
        this.holdExpiresAt = holdExpiresAt;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public static Reservation createPendingPayment(
        String reservationNo,
        User user,
        TrainSchedule trainSchedule,
        BigDecimal totalAmount,
        LocalDateTime holdExpiresAt
    ) {
        return new Reservation(reservationNo, user, trainSchedule, totalAmount, holdExpiresAt);
    }

    public void confirm(LocalDateTime now) {
        this.status = ReservationStatus.CONFIRMED;
        this.updatedAt = now;
    }

    public void failPayment(LocalDateTime now) {
        this.status = ReservationStatus.PAYMENT_FAILED;
        this.updatedAt = now;
    }

    public void cancel(LocalDateTime now) {
        this.status = ReservationStatus.CANCELED;
        this.updatedAt = now;
    }

    public void expire(LocalDateTime now) {
        this.status = ReservationStatus.EXPIRED;
        this.updatedAt = now;
    }

    public Long getId() {
        return id;
    }

    public String getReservationNo() {
        return reservationNo;
    }

    public User getUser() {
        return user;
    }

    public TrainSchedule getTrainSchedule() {
        return trainSchedule;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public LocalDateTime getHoldExpiresAt() {
        return holdExpiresAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}