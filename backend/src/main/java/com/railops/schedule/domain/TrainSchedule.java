package com.railops.schedule.domain;

import com.railops.route.domain.Route;
import com.railops.train.domain.Train;
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
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "train_schedules")
public class TrainSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "train_id", nullable = false)
    private Train train;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;

    @Column(nullable = false)
    private LocalDate operationDate;

    @Column(nullable = false)
    private LocalDateTime departureTime;

    @Column(nullable = false)
    private LocalDateTime arrivalTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TrainScheduleStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    protected TrainSchedule() {
    }

    private TrainSchedule(
        Train train,
        Route route,
        LocalDate operationDate,
        LocalDateTime departureTime,
        LocalDateTime arrivalTime
    ) {
        LocalDateTime now = LocalDateTime.now();
        this.train = train;
        this.route = route;
        this.operationDate = operationDate;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.status = TrainScheduleStatus.SCHEDULED;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public static TrainSchedule create(
        Train train,
        Route route,
        LocalDate operationDate,
        LocalDateTime departureTime,
        LocalDateTime arrivalTime
    ) {
        return new TrainSchedule(train, route, operationDate, departureTime, arrivalTime);
    }

    public void update(Route route, LocalDate operationDate, LocalDateTime departureTime, LocalDateTime arrivalTime) {
        this.route = route;
        this.operationDate = operationDate;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateStatus(TrainScheduleStatus status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Train getTrain() {
        return train;
    }

    public Route getRoute() {
        return route;
    }

    public LocalDate getOperationDate() {
        return operationDate;
    }

    public LocalDateTime getDepartureTime() {
        return departureTime;
    }

    public LocalDateTime getArrivalTime() {
        return arrivalTime;
    }

    public TrainScheduleStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}