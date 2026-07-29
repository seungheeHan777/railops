package com.railops.route.service;

import com.railops.common.error.BusinessException;
import com.railops.common.error.ErrorCode;
import com.railops.route.domain.Route;
import com.railops.route.dto.RouteCreateRequest;
import com.railops.route.dto.RouteResponse;
import com.railops.route.dto.RouteUpdateRequest;
import com.railops.route.repository.RouteRepository;
import com.railops.station.domain.Station;
import com.railops.station.repository.StationRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class RouteService {

    private final RouteRepository routeRepository;
    private final StationRepository stationRepository;

    public RouteService(RouteRepository routeRepository, StationRepository stationRepository) {
        this.routeRepository = routeRepository;
        this.stationRepository = stationRepository;
    }

    public List<RouteResponse> findAll() {
        return routeRepository.findAll()
            .stream()
            .map(RouteResponse::from)
            .toList();
    }

    public RouteResponse get(Long routeId) {
        return RouteResponse.from(getRoute(routeId));
    }

    @Transactional
    public RouteResponse create(RouteCreateRequest request) {
        validateDifferentStations(request.originStationId(), request.destinationStationId());
        validateDuplicateRoute(request.name(), request.originStationId(), request.destinationStationId());

        Station originStation = getStation(request.originStationId());
        Station destinationStation = getStation(request.destinationStationId());
        Route route = Route.create(request.name(), originStation, destinationStation);
        return RouteResponse.from(routeRepository.save(route));
    }

    @Transactional
    public RouteResponse update(Long routeId, RouteUpdateRequest request) {
        Route route = getRoute(routeId);
        validateDifferentStations(request.originStationId(), request.destinationStationId());

        if (routeRepository.existsByNameAndOriginStation_IdAndDestinationStation_IdAndIdNot(
            request.name(),
            request.originStationId(),
            request.destinationStationId(),
            routeId
        )) {
            throw new BusinessException(ErrorCode.DUPLICATE_ROUTE);
        }

        Station originStation = getStation(request.originStationId());
        Station destinationStation = getStation(request.destinationStationId());
        route.update(request.name(), originStation, destinationStation);
        return RouteResponse.from(route);
    }

    @Transactional
    public void delete(Long routeId) {
        Route route = getRoute(routeId);
        routeRepository.delete(route);
    }

    private Route getRoute(Long routeId) {
        return routeRepository.findById(routeId)
            .orElseThrow(() -> new BusinessException(ErrorCode.ROUTE_NOT_FOUND));
    }

    private Station getStation(Long stationId) {
        return stationRepository.findById(stationId)
            .orElseThrow(() -> new BusinessException(ErrorCode.STATION_NOT_FOUND));
    }

    private void validateDifferentStations(Long originStationId, Long destinationStationId) {
        if (originStationId.equals(destinationStationId)) {
            throw new BusinessException(ErrorCode.INVALID_ROUTE_STATIONS);
        }
    }

    private void validateDuplicateRoute(String name, Long originStationId, Long destinationStationId) {
        if (routeRepository.existsByNameAndOriginStation_IdAndDestinationStation_Id(
            name,
            originStationId,
            destinationStationId
        )) {
            throw new BusinessException(ErrorCode.DUPLICATE_ROUTE);
        }
    }
}