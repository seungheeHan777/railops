package com.railops.train.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "cars",
    uniqueConstraints = @UniqueConstraint(name = "uk_cars_train_car_no", columnNames = {"train_id", "car_no"})
)
public class Car {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "train_id", nullable = false)
    private Train train;

    @Column(nullable = false)
    private Integer carNo;

    @Column(nullable = false)
    private Integer seatCount;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    protected Car() {
    }

    private Car(Train train, Integer carNo, Integer seatCount) {
        LocalDateTime now = LocalDateTime.now();
        this.train = train;
        this.carNo = carNo;
        this.seatCount = seatCount;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public static Car create(Train train, Integer carNo, Integer seatCount) {
        return new Car(train, carNo, seatCount);
    }

    public void update(Integer carNo, Integer seatCount) {
        this.carNo = carNo;
        this.seatCount = seatCount;
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Train getTrain() {
        return train;
    }

    public Integer getCarNo() {
        return carNo;
    }

    public Integer getSeatCount() {
        return seatCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}