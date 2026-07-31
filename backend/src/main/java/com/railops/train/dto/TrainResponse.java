package com.railops.train.dto;

import com.railops.train.domain.Train;

public record TrainResponse(
    Long id,
    String trainNo,
    String trainType,
    String name
) {

    public static TrainResponse from(Train train) {
        return new TrainResponse(
            train.getId(),
            train.getTrainNo(),
            train.getTrainType(),
            train.getName()
        );
    }
}