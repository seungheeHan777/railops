package com.railops.seat.dto;

import java.util.List;

public record ScheduleSeatMapResponse(
    Long scheduleId,
    List<CarSeatResponse> cars
) {
}