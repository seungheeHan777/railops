package com.railops.schedule.repository;

import com.railops.schedule.domain.TrainSchedule;
import com.railops.schedule.domain.TrainScheduleStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrainScheduleRepository extends JpaRepository<TrainSchedule, Long> {

    List<TrainSchedule> findAllByOrderByDepartureTimeAsc();

    List<TrainSchedule> findByRoute_OriginStation_CodeAndRoute_DestinationStation_CodeAndOperationDateAndStatusInOrderByDepartureTimeAsc(
        String originStationCode,
        String destinationStationCode,
        LocalDate operationDate,
        Collection<TrainScheduleStatus> statuses
    );

    boolean existsByTrain_IdAndDepartureTimeLessThanAndArrivalTimeGreaterThan(
        Long trainId,
        LocalDateTime arrivalTime,
        LocalDateTime departureTime
    );

    boolean existsByTrain_IdAndDepartureTimeLessThanAndArrivalTimeGreaterThanAndIdNot(
        Long trainId,
        LocalDateTime arrivalTime,
        LocalDateTime departureTime,
        Long id
    );
}