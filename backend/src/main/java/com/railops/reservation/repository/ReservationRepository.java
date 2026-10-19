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
    boolean existsByTrainSchedule_Id(Long id);

    @Query("""
        select (count(r) > 0) from Reservation r where r.user.id = :id and
        ((r.status = com.railops.reservation.domain.ReservationStatus.PENDING_PAYMENT and r.holdExpiresAt > :now)
        or (r.status = com.railops.reservation.domain.ReservationStatus.CONFIRMED and r.trainSchedule.departureTime > :now))
        """)
    boolean hasActiveReservations(@Param("id") Long id, @Param("now") java.time.LocalDateTime now);
    @EntityGraph(attributePaths = {"user", "trainSchedule", "trainSchedule.train",
        "trainSchedule.route", "trainSchedule.route.originStation", "trainSchedule.route.destinationStation"})
    @Query("""
        select r from Reservation r
        where (:userId is null or r.user.id = :userId)
          and (:status is null or r.status = :status)
        """)
    Page<Reservation> findPage(@Param("userId") Long userId, @Param("status") ReservationStatus status, Pageable pageable);
}
