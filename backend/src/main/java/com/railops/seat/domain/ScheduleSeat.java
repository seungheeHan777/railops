package com.railops.seat.domain;

import com.railops.schedule.domain.TrainSchedule;
import com.railops.train.domain.Seat;
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
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "schedule_seats",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_schedule_seats_schedule_seat",
        columnNames = {"train_schedule_id", "seat_id"}
    )
)
public class ScheduleSeat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "train_schedule_id", nullable = false)
    private TrainSchedule trainSchedule;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ScheduleSeatStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "held_by_user_id")
    private User heldByUser;

    private LocalDateTime holdExpiresAt;

    @Version
    private Long version;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    protected ScheduleSeat() {
    }

    private ScheduleSeat(TrainSchedule trainSchedule, Seat seat) {
        LocalDateTime now = LocalDateTime.now();
        this.trainSchedule = trainSchedule;
        this.seat = seat;
        this.status = ScheduleSeatStatus.AVAILABLE;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public static ScheduleSeat createAvailable(TrainSchedule trainSchedule, Seat seat) {
        return new ScheduleSeat(trainSchedule, seat);
    }

    public void block() {
        this.status = ScheduleSeatStatus.BLOCKED;
        this.heldByUser = null;
        this.holdExpiresAt = null;
        this.updatedAt = LocalDateTime.now();
    }

    public void unblock() {
        releaseHold();
    }

    public void hold(User user, LocalDateTime holdExpiresAt) {
        this.status = ScheduleSeatStatus.HELD;
        this.heldByUser = user;
        this.holdExpiresAt = holdExpiresAt;
        this.updatedAt = LocalDateTime.now();
    }

    public void reserve() {
        this.status = ScheduleSeatStatus.RESERVED;
        this.heldByUser = null;
        this.holdExpiresAt = null;
        this.updatedAt = LocalDateTime.now();
    }

    public void releaseHold() {
        this.status = ScheduleSeatStatus.AVAILABLE;
        this.heldByUser = null;
        this.holdExpiresAt = null;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isExpiredHold(LocalDateTime now) {
        return status == ScheduleSeatStatus.HELD
            && holdExpiresAt != null
            && holdExpiresAt.isBefore(now);
    }

    public Long getId() {
        return id;
    }

    public TrainSchedule getTrainSchedule() {
        return trainSchedule;
    }

    public Seat getSeat() {
        return seat;
    }

    public ScheduleSeatStatus getStatus() {
        return status;
    }

    public User getHeldByUser() {
        return heldByUser;
    }

    public LocalDateTime getHoldExpiresAt() {
        return holdExpiresAt;
    }

    public Long getVersion() {
        return version;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}