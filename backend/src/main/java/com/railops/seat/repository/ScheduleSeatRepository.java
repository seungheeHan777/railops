package com.railops.seat.repository;

import com.railops.seat.domain.ScheduleSeat;
import java.time.LocalDateTime;
import java.util.List;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ScheduleSeatRepository extends JpaRepository<ScheduleSeat, Long> {

    boolean existsByTrainSchedule_Id(Long scheduleId);

    @Query("""
        select ss
        from ScheduleSeat ss
        join fetch ss.seat seat
        join fetch seat.car car
        where ss.trainSchedule.id = :scheduleId
        order by car.carNo asc, seat.seatNo asc
        """)
    List<ScheduleSeat> findSeatMapRows(@Param("scheduleId") Long scheduleId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select ss
        from ScheduleSeat ss
        join fetch ss.trainSchedule schedule
        join fetch ss.seat seat
        join fetch seat.car car
        where ss.id in :ids
        order by car.carNo asc, seat.seatNo asc
        """)
    List<ScheduleSeat> findAllByIdInForUpdate(@Param("ids") List<Long> ids);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update ScheduleSeat ss
        set ss.status = com.railops.seat.domain.ScheduleSeatStatus.AVAILABLE,
            ss.heldByUser = null,
            ss.holdExpiresAt = null,
            ss.updatedAt = :now
        where ss.status = com.railops.seat.domain.ScheduleSeatStatus.HELD
          and ss.holdExpiresAt < :now
        """)
    int releaseExpiredHolds(@Param("now") LocalDateTime now);
}