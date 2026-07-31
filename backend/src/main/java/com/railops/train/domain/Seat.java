package com.railops.train.domain;

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
import java.time.LocalDateTime;

@Entity
@Table(
    name = "seats",
    uniqueConstraints = @UniqueConstraint(name = "uk_seats_car_seat_no", columnNames = {"car_id", "seat_no"})
)
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "car_id", nullable = false)
    private Car car;

    @Column(nullable = false)
    private String seatNo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeatType seatType;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    protected Seat() {
    }

    private Seat(Car car, String seatNo, SeatType seatType) {
        LocalDateTime now = LocalDateTime.now();
        this.car = car;
        this.seatNo = normalizeSeatNo(seatNo);
        this.seatType = seatType;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public static Seat create(Car car, String seatNo, SeatType seatType) {
        return new Seat(car, seatNo, seatType);
    }

    public void update(String seatNo, SeatType seatType) {
        this.seatNo = normalizeSeatNo(seatNo);
        this.seatType = seatType;
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Car getCar() {
        return car;
    }

    public String getSeatNo() {
        return seatNo;
    }

    public SeatType getSeatType() {
        return seatType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    private static String normalizeSeatNo(String seatNo) {
        return seatNo == null ? null : seatNo.trim().toUpperCase();
    }
}