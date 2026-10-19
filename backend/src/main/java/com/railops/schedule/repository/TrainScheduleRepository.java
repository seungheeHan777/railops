package com.railops.schedule.repository;

import com.railops.schedule.domain.TrainSchedule;
import com.railops.schedule.domain.TrainScheduleStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrainScheduleRepository extends JpaRepository<TrainSchedule, Long> {
    @org.springframework.data.jpa.repository.Query("select s.train.id from TrainSchedule s where s.id = :id")
    java.util.Optional<Long> findTrainId(@org.springframework.data.repository.query.Param("id") Long id);
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_READ)
    @org.springframework.data.jpa.repository.Query("select s from TrainSchedule s where s.id = :id")
    java.util.Optional<TrainSchedule> findByIdForShare(@org.springframework.data.repository.query.Param("id") Long id);

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select s from TrainSchedule s where s.id = :id")
    java.util.Optional<TrainSchedule> findByIdForUpdate(@org.springframework.data.repository.query.Param("id") Long id);

    List<TrainSchedule> findAllByOrderByDepartureTimeAsc();

    List<TrainSchedule> findByRoute_OriginStation_CodeAndRoute_DestinationStation_CodeAndOperationDateAndStatusInOrderByDepartureTimeAsc(
        String originStationCode,
        String destinationStationCode,
        LocalDate operationDate,
        Collection<TrainScheduleStatus> statuses
    );

    @org.springframework.data.jpa.repository.Query("""
        select (count(s) > 0) from TrainSchedule s where s.train.id = :trainId
        and s.departureTime < :arrivalTime and s.arrivalTime > :departureTime
        and s.status <> com.railops.schedule.domain.TrainScheduleStatus.CANCELED
        """)
    boolean existsByTrain_IdAndDepartureTimeLessThanAndArrivalTimeGreaterThan(
        @org.springframework.data.repository.query.Param("trainId") Long trainId,
        @org.springframework.data.repository.query.Param("arrivalTime") LocalDateTime arrivalTime,
        @org.springframework.data.repository.query.Param("departureTime") LocalDateTime departureTime
    );

    @org.springframework.data.jpa.repository.Query("""
        select (count(s) > 0) from TrainSchedule s where s.train.id = :trainId
        and s.departureTime < :arrivalTime and s.arrivalTime > :departureTime and s.id <> :id
        and s.status <> com.railops.schedule.domain.TrainScheduleStatus.CANCELED
        """)
    boolean existsByTrain_IdAndDepartureTimeLessThanAndArrivalTimeGreaterThanAndIdNot(
        @org.springframework.data.repository.query.Param("trainId") Long trainId,
        @org.springframework.data.repository.query.Param("arrivalTime") LocalDateTime arrivalTime,
        @org.springframework.data.repository.query.Param("departureTime") LocalDateTime departureTime,
        @org.springframework.data.repository.query.Param("id") Long id
    );
}
