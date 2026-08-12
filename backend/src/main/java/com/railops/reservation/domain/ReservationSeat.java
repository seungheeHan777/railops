package com.railops.reservation.domain;

import com.railops.seat.domain.ScheduleSeat;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "reservation_seats")
public class ReservationSeat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "schedule_seat_id", nullable = false)
    private ScheduleSeat scheduleSeat;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    protected ReservationSeat() {
    }

    private ReservationSeat(Reservation reservation, ScheduleSeat scheduleSeat, BigDecimal price) {
        this.reservation = reservation;
        this.scheduleSeat = scheduleSeat;
        this.price = price;
        this.createdAt = LocalDateTime.now();
    }

    public static ReservationSeat create(Reservation reservation, ScheduleSeat scheduleSeat, BigDecimal price) {
        return new ReservationSeat(reservation, scheduleSeat, price);
    }

    public Long getId() {
        return id;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public ScheduleSeat getScheduleSeat() {
        return scheduleSeat;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}