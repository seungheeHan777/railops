package com.railops.schedule.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.railops.common.error.BusinessException;
import com.railops.common.error.ErrorCode;
import com.railops.route.domain.Route;
import com.railops.route.repository.RouteRepository;
import com.railops.schedule.domain.TrainSchedule;
import com.railops.schedule.domain.TrainScheduleStatus;
import com.railops.schedule.dto.TrainScheduleCreateRequest;
import com.railops.schedule.dto.TrainScheduleResponse;
import com.railops.schedule.dto.TrainScheduleSearchResponse;
import com.railops.schedule.dto.TrainScheduleStatusUpdateRequest;
import com.railops.schedule.dto.TrainScheduleUpdateRequest;
import com.railops.schedule.repository.TrainScheduleRepository;
import com.railops.seat.service.ScheduleSeatService;
import com.railops.station.domain.Station;
import com.railops.train.domain.Train;
import com.railops.train.repository.TrainRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class TrainScheduleServiceTest {

    @Mock
    private TrainScheduleRepository trainScheduleRepository;

    @Mock
    private TrainRepository trainRepository;

    @Mock
    private RouteRepository routeRepository;

    @Mock
    private ScheduleSeatService scheduleSeatService;

    private TrainScheduleService trainScheduleService;

    @BeforeEach
    void setUp() {
        trainScheduleService = new TrainScheduleService(
            trainScheduleRepository,
            trainRepository,
            routeRepository,
            scheduleSeatService
        );
    }

    @Test
    void createSchedule() {
        Train train = trainWithId(1L);
        Route route = routeWithId(2L);
        TrainScheduleCreateRequest request = new TrainScheduleCreateRequest(
            1L,
            2L,
            LocalDate.of(2026, 8, 1),
            LocalDateTime.of(2026, 8, 1, 9, 0),
            LocalDateTime.of(2026, 8, 1, 11, 40)
        );
        TrainSchedule schedule = scheduleWithId(10L, train, route, request.departureTime(), request.arrivalTime());

        when(trainScheduleRepository.existsByTrain_IdAndDepartureTimeLessThanAndArrivalTimeGreaterThan(
            1L,
            request.arrivalTime(),
            request.departureTime()
        )).thenReturn(false);
        when(trainRepository.findById(1L)).thenReturn(Optional.of(train));
        when(routeRepository.findById(2L)).thenReturn(Optional.of(route));
        when(trainScheduleRepository.save(any(TrainSchedule.class))).thenReturn(schedule);

        TrainScheduleResponse response = trainScheduleService.create(request);

        assertThat(response.trainNo()).isEqualTo("KTX-101");
        assertThat(response.routeName()).isEqualTo("경부선");
        assertThat(response.status()).isEqualTo(TrainScheduleStatus.SCHEDULED);
        verify(scheduleSeatService).createAvailableSeatsForSchedule(schedule);
    }

    @Test
    void createScheduleRejectsInvalidTime() {
        TrainScheduleCreateRequest request = new TrainScheduleCreateRequest(
            1L,
            2L,
            LocalDate.of(2026, 8, 1),
            LocalDateTime.of(2026, 8, 1, 12, 0),
            LocalDateTime.of(2026, 8, 1, 11, 0)
        );

        assertThatThrownBy(() -> trainScheduleService.create(request))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.INVALID_SCHEDULE_TIME);
    }

    @Test
    void createScheduleRejectsTrainTimeConflict() {
        TrainScheduleCreateRequest request = new TrainScheduleCreateRequest(
            1L,
            2L,
            LocalDate.of(2026, 8, 1),
            LocalDateTime.of(2026, 8, 1, 9, 0),
            LocalDateTime.of(2026, 8, 1, 11, 40)
        );

        when(trainScheduleRepository.existsByTrain_IdAndDepartureTimeLessThanAndArrivalTimeGreaterThan(
            1L,
            request.arrivalTime(),
            request.departureTime()
        )).thenReturn(true);

        assertThatThrownBy(() -> trainScheduleService.create(request))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.TRAIN_SCHEDULE_CONFLICT);
    }

    @Test
    void createScheduleRejectsMissingTrain() {
        TrainScheduleCreateRequest request = new TrainScheduleCreateRequest(
            1L,
            2L,
            LocalDate.of(2026, 8, 1),
            LocalDateTime.of(2026, 8, 1, 9, 0),
            LocalDateTime.of(2026, 8, 1, 11, 40)
        );

        when(trainScheduleRepository.existsByTrain_IdAndDepartureTimeLessThanAndArrivalTimeGreaterThan(
            1L,
            request.arrivalTime(),
            request.departureTime()
        )).thenReturn(false);
        when(trainRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainScheduleService.create(request))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.TRAIN_NOT_FOUND);
    }

    @Test
    void searchSchedules() {
        Train train = trainWithId(1L);
        Route route = routeWithId(2L);
        TrainSchedule schedule = scheduleWithId(
            10L,
            train,
            route,
            LocalDateTime.of(2026, 8, 1, 9, 0),
            LocalDateTime.of(2026, 8, 1, 11, 40)
        );

        when(trainScheduleRepository
            .findByRoute_OriginStation_CodeAndRoute_DestinationStation_CodeAndOperationDateAndStatusInOrderByDepartureTimeAsc(
                "SEOUL",
                "BUSAN",
                LocalDate.of(2026, 8, 1),
                List.of(TrainScheduleStatus.SCHEDULED, TrainScheduleStatus.DELAYED)
            )).thenReturn(List.of(schedule));

        List<TrainScheduleSearchResponse> responses = trainScheduleService.search("seoul", "busan", LocalDate.of(2026, 8, 1));

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).scheduleId()).isEqualTo(10L);
        assertThat(responses.get(0).origin()).isEqualTo("서울");
    }

    @Test
    void getRejectsMissingSchedule() {
        when(trainScheduleRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainScheduleService.get(10L))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.SCHEDULE_NOT_FOUND);
    }

    @Test
    void updateSchedule() {
        Train train = trainWithId(1L);
        Route route = routeWithId(2L);
        Route nextRoute = routeWithId(3L);
        TrainSchedule schedule = scheduleWithId(
            10L,
            train,
            route,
            LocalDateTime.of(2026, 8, 1, 9, 0),
            LocalDateTime.of(2026, 8, 1, 11, 40)
        );
        TrainScheduleUpdateRequest request = new TrainScheduleUpdateRequest(
            3L,
            LocalDate.of(2026, 8, 1),
            LocalDateTime.of(2026, 8, 1, 10, 0),
            LocalDateTime.of(2026, 8, 1, 12, 30)
        );

        when(trainScheduleRepository.findById(10L)).thenReturn(Optional.of(schedule));
        when(trainScheduleRepository.existsByTrain_IdAndDepartureTimeLessThanAndArrivalTimeGreaterThanAndIdNot(
            1L,
            request.arrivalTime(),
            request.departureTime(),
            10L
        )).thenReturn(false);
        when(routeRepository.findById(3L)).thenReturn(Optional.of(nextRoute));

        TrainScheduleResponse response = trainScheduleService.update(10L, request);

        assertThat(response.routeId()).isEqualTo(3L);
        assertThat(response.departureTime()).isEqualTo(request.departureTime());
    }

    @Test
    void updateStatus() {
        Train train = trainWithId(1L);
        Route route = routeWithId(2L);
        TrainSchedule schedule = scheduleWithId(
            10L,
            train,
            route,
            LocalDateTime.of(2026, 8, 1, 9, 0),
            LocalDateTime.of(2026, 8, 1, 11, 40)
        );

        when(trainScheduleRepository.findById(10L)).thenReturn(Optional.of(schedule));

        TrainScheduleResponse response = trainScheduleService.updateStatus(
            10L,
            new TrainScheduleStatusUpdateRequest(TrainScheduleStatus.CANCELED)
        );

        assertThat(response.status()).isEqualTo(TrainScheduleStatus.CANCELED);
    }

    @Test
    void deleteSchedule() {
        Train train = trainWithId(1L);
        Route route = routeWithId(2L);
        TrainSchedule schedule = scheduleWithId(
            10L,
            train,
            route,
            LocalDateTime.of(2026, 8, 1, 9, 0),
            LocalDateTime.of(2026, 8, 1, 11, 40)
        );
        when(trainScheduleRepository.findById(10L)).thenReturn(Optional.of(schedule));

        trainScheduleService.delete(10L);

        verify(trainScheduleRepository).delete(schedule);
    }

    private Train trainWithId(Long id) {
        Train train = Train.create("KTX-101", "KTX", "KTX 101");
        ReflectionTestUtils.setField(train, "id", id);
        return train;
    }

    private Route routeWithId(Long id) {
        Station seoul = stationWithId(100L, "서울", "SEOUL", "서울");
        Station busan = stationWithId(200L, "부산", "BUSAN", "부산");
        Route route = Route.create("경부선", seoul, busan);
        ReflectionTestUtils.setField(route, "id", id);
        return route;
    }

    private Station stationWithId(Long id, String name, String code, String city) {
        Station station = Station.create(name, code, city);
        ReflectionTestUtils.setField(station, "id", id);
        return station;
    }

    private TrainSchedule scheduleWithId(
        Long id,
        Train train,
        Route route,
        LocalDateTime departureTime,
        LocalDateTime arrivalTime
    ) {
        TrainSchedule schedule = TrainSchedule.create(
            train,
            route,
            departureTime.toLocalDate(),
            departureTime,
            arrivalTime
        );
        ReflectionTestUtils.setField(schedule, "id", id);
        return schedule;
    }
}