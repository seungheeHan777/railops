package com.railops.reservation.repository;

import com.railops.reservation.domain.ReservationSeat;
import java.util.List;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReservationSeatRepository extends JpaRepository<ReservationSeat, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select reservationSeat
        from ReservationSeat reservationSeat
        join fetch reservationSeat.scheduleSeat scheduleSeat
        left join fetch scheduleSeat.heldByUser heldByUser
        where reservationSeat.reservation.id = :reservationId
        order by scheduleSeat.id asc
        """)
    List<ReservationSeat> findByReservationIdForPayment(@Param("reservationId") Long reservationId);
}