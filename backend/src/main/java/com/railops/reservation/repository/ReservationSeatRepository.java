package com.railops.reservation.repository;

import com.railops.reservation.domain.ReservationSeat;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReservationSeatRepository extends JpaRepository<ReservationSeat, Long> {
    @Query("select rs.scheduleSeat.id from ReservationSeat rs where rs.reservation.id = :reservationId order by rs.scheduleSeat.id")
    List<Long> findScheduleSeatIds(@Param("reservationId") Long reservationId);

    @Query("""
        select rs from ReservationSeat rs
        join fetch rs.scheduleSeat ss join fetch ss.seat s join fetch s.car
        where rs.reservation.id = :reservationId order by ss.id
        """)
    List<ReservationSeat> findByReservationIdForPayment(@Param("reservationId") Long reservationId);
}
