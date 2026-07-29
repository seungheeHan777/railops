package com.railops.route.repository;

import com.railops.route.domain.Route;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RouteRepository extends JpaRepository<Route, Long> {

    boolean existsByNameAndOriginStation_IdAndDestinationStation_Id(
        String name,
        Long originStationId,
        Long destinationStationId
    );

    boolean existsByNameAndOriginStation_IdAndDestinationStation_IdAndIdNot(
        String name,
        Long originStationId,
        Long destinationStationId,
        Long id
    );
}