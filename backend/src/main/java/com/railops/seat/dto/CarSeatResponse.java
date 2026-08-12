package com.railops.seat.dto;

import java.util.List;

public record CarSeatResponse(
    Long carId,
    Integer carNo,
    List<ScheduleSeatResponse> seats
) {
}