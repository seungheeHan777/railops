package com.railops.schedule.dto;

import com.railops.schedule.domain.TrainSchedule;
import com.railops.schedule.domain.TrainScheduleStatus;
import java.time.LocalDateTime;

public record TrainScheduleSearchResponse(
    Long scheduleId,
    String trainNo,
    String trainType,
    String routeName,
    String origin,
    String destination,
    LocalDateTime departureTime,
    LocalDateTime arrivalTime,
    TrainScheduleStatus status
) {

    public static TrainScheduleSearchResponse from(TrainSchedule schedule) {
        return new TrainScheduleSearchResponse(
            schedule.getId(),
            schedule.getTrain().getTrainNo(),
            schedule.getTrain().getTrainType(),
            schedule.getRoute().getName(),
            schedule.getRoute().getOriginStation().getName(),
            schedule.getRoute().getDestinationStation().getName(),
            schedule.getDepartureTime(),
            schedule.getArrivalTime(),
            schedule.getStatus()
        );
    }
}