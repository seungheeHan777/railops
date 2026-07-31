package com.railops.train.dto;

import com.railops.train.domain.Seat;
import com.railops.train.domain.SeatType;

public record SeatResponse(
    Long id,
    Long carId,
    Long trainId,
    Integer carNo,
    String seatNo,
    SeatType seatType
) {

    public static SeatResponse from(Seat seat) {
        return new SeatResponse(
            seat.getId(),
            seat.getCar().getId(),
            seat.getCar().getTrain().getId(),
            seat.getCar().getCarNo(),
            seat.getSeatNo(),
            seat.getSeatType()
        );
    }
}