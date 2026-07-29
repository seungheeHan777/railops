package com.railops.route.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RouteServiceTest {

    @Mock
    private RouteRepository routeRepository;

    @Mock
    private StationRepository stationRepository;

    private RouteService routeService;

    @BeforeEach
    void setUp() {
        routeService = new RouteService(routeRepository, stationRepository);
    }

    @Test
    void createRoute() {
        Station seoul = Station.create("서울", "SEOUL", "서울");
        Station busan = Station.create("부산", "BUSAN", "부산");
        RouteCreateRequest request = new RouteCreateRequest("경부선", 1L, 2L);
        Route savedRoute = Route.create(request.name(), seoul, busan);

        when(routeRepository.existsByNameAndOriginStation_IdAndDestinationStation_Id("경부선", 1L, 2L))
            .thenReturn(false);
        when(stationRepository.findById(1L)).thenReturn(Optional.of(seoul));
        when(stationRepository.findById(2L)).thenReturn(Optional.of(busan));
        when(routeRepository.save(any(Route.class))).thenReturn(savedRoute);

        RouteResponse response = routeService.create(request);

        assertThat(response.name()).isEqualTo("경부선");
        assertThat(response.originStationName()).isEqualTo("서울");
        assertThat(response.destinationStationName()).isEqualTo("부산");
    }

    @Test
    void createRouteRejectsSameOriginAndDestination() {
        RouteCreateRequest request = new RouteCreateRequest("순환선", 1L, 1L);

        assertThatThrownBy(() -> routeService.create(request))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.INVALID_ROUTE_STATIONS);
    }

    @Test
    void createRouteRejectsDuplicateRoute() {
        RouteCreateRequest request = new RouteCreateRequest("경부선", 1L, 2L);
        when(routeRepository.existsByNameAndOriginStation_IdAndDestinationStation_Id("경부선", 1L, 2L))
            .thenReturn(true);

        assertThatThrownBy(() -> routeService.create(request))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.DUPLICATE_ROUTE);
    }

    @Test
    void createRouteRejectsMissingStation() {
        RouteCreateRequest request = new RouteCreateRequest("경부선", 1L, 2L);
        when(routeRepository.existsByNameAndOriginStation_IdAndDestinationStation_Id("경부선", 1L, 2L))
            .thenReturn(false);
        when(stationRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> routeService.create(request))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.STATION_NOT_FOUND);
    }

    @Test
    void findAllRoutes() {
        Station seoul = Station.create("서울", "SEOUL", "서울");
        Station busan = Station.create("부산", "BUSAN", "부산");
        when(routeRepository.findAll()).thenReturn(List.of(Route.create("경부선", seoul, busan)));

        List<RouteResponse> responses = routeService.findAll();

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).name()).isEqualTo("경부선");
    }

    @Test
    void getRejectsMissingRoute() {
        when(routeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> routeService.get(1L))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.ROUTE_NOT_FOUND);
    }

    @Test
    void updateRoute() {
        Station seoul = Station.create("서울", "SEOUL", "서울");
        Station busan = Station.create("부산", "BUSAN", "부산");
        Station suseo = Station.create("수서", "SUSEO", "서울");
        Route route = Route.create("경부선", seoul, busan);
        RouteUpdateRequest request = new RouteUpdateRequest("수서부산선", 3L, 2L);

        when(routeRepository.findById(1L)).thenReturn(Optional.of(route));
        when(routeRepository.existsByNameAndOriginStation_IdAndDestinationStation_IdAndIdNot("수서부산선", 3L, 2L, 1L))
            .thenReturn(false);
        when(stationRepository.findById(3L)).thenReturn(Optional.of(suseo));
        when(stationRepository.findById(2L)).thenReturn(Optional.of(busan));

        RouteResponse response = routeService.update(1L, request);

        assertThat(response.name()).isEqualTo("수서부산선");
        assertThat(response.originStationName()).isEqualTo("수서");
    }

    @Test
    void deleteRoute() {
        Station seoul = Station.create("서울", "SEOUL", "서울");
        Station busan = Station.create("부산", "BUSAN", "부산");
        Route route = Route.create("경부선", seoul, busan);
        when(routeRepository.findById(1L)).thenReturn(Optional.of(route));

        routeService.delete(1L);

        verify(routeRepository).delete(route);
    }
}