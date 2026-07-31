package com.railops.train.dto;

import com.railops.train.domain.Car;

public record CarResponse(
    Long id,
    Long trainId,
    String trainNo,
    String trainType,
    Integer carNo,
    Integer seatCount
) {

    public static CarResponse from(Car car) {
        return new CarResponse(
            car.getId(),
            car.getTrain().getId(),
            car.getTrain().getTrainNo(),
            car.getTrain().getTrainType(),
            car.getCarNo(),
            car.getSeatCount()
        );
    }
}