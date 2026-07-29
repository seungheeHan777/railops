package com.railops.station.dto;

import com.railops.station.domain.Station;

public record StationResponse(
    Long id,
    String name,
    String code,
    String city
) {

    public static StationResponse from(Station station) {
        return new StationResponse(
            station.getId(),
            station.getName(),
            station.getCode(),
            station.getCity()
        );
    }
}