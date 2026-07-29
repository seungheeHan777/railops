package com.railops.route.dto;

import com.railops.route.domain.Route;

public record RouteResponse(
    Long id,
    String name,
    Long originStationId,
    String originStationName,
    String originStationCode,
    Long destinationStationId,
    String destinationStationName,
    String destinationStationCode
) {

    public static RouteResponse from(Route route) {
        return new RouteResponse(
            route.getId(),
            route.getName(),
            route.getOriginStation().getId(),
            route.getOriginStation().getName(),
            route.getOriginStation().getCode(),
            route.getDestinationStation().getId(),
            route.getDestinationStation().getName(),
            route.getDestinationStation().getCode()
        );
    }
}