package com.railops.reservation.repository;

import com.railops.reservation.domain.Reservation;
import com.railops.reservation.domain.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    @EntityGraph(attributePaths = {"user", "trainSchedule", "trainSchedule.train",
        "trainSchedule.route", "trainSchedule.route.originStation", "trainSchedule.route.destinationStation"})
    @Query("""
        select r from Reservation r
        where (:userId is null or r.user.id = :userId)
          and (:status is null or r.status = :status)
        """)
    Page<Reservation> findPage(@Param("userId") Long userId, @Param("status") ReservationStatus status, Pageable pageable);
}
