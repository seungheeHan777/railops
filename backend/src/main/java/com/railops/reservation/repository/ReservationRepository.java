package com.railops.reservation.repository;

import com.railops.reservation.domain.Reservation;
import com.railops.reservation.domain.ReservationStatus;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update Reservation reservation
        set reservation.status = :expiredStatus,
            reservation.updatedAt = :now
        where reservation.status = :pendingStatus
          and reservation.holdExpiresAt < :now
        """)
    int expirePendingReservations(
        @Param("pendingStatus") ReservationStatus pendingStatus,
        @Param("expiredStatus") ReservationStatus expiredStatus,
        @Param("now") LocalDateTime now
    );
}