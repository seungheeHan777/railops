package com.railops.schedule.dto;

import com.railops.schedule.domain.TrainSchedule;
import com.railops.schedule.domain.TrainScheduleStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record TrainScheduleResponse(
    Long id,
    Long trainId,
    String trainNo,
    String trainType,
    Long routeId,
    String routeName,
    Long originStationId,
    String originStationName,
    String originStationCode,
    Long destinationStationId,
    String destinationStationName,
    String destinationStationCode,
    LocalDate operationDate,
    LocalDateTime departureTime,
    LocalDateTime arrivalTime,
    TrainScheduleStatus status
) {

    public static TrainScheduleResponse from(TrainSchedule schedule) {
        return new TrainScheduleResponse(
            schedule.getId(),
            schedule.getTrain().getId(),
            schedule.getTrain().getTrainNo(),
            schedule.getTrain().getTrainType(),
            schedule.getRoute().getId(),
            schedule.getRoute().getName(),
            schedule.getRoute().getOriginStation().getId(),
            schedule.getRoute().getOriginStation().getName(),
            schedule.getRoute().getOriginStation().getCode(),
            schedule.getRoute().getDestinationStation().getId(),
            schedule.getRoute().getDestinationStation().getName(),
            schedule.getRoute().getDestinationStation().getCode(),
            schedule.getOperationDate(),
            schedule.getDepartureTime(),
            schedule.getArrivalTime(),
            schedule.getStatus()
        );
    }
}