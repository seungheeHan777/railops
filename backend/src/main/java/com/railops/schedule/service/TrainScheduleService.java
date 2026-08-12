package com.railops.schedule.service;

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
import com.railops.train.domain.Train;
import com.railops.train.repository.TrainRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class TrainScheduleService {

    private static final List<TrainScheduleStatus> BOOKABLE_SEARCH_STATUSES = List.of(
        TrainScheduleStatus.SCHEDULED,
        TrainScheduleStatus.DELAYED
    );

    private final TrainScheduleRepository trainScheduleRepository;
    private final TrainRepository trainRepository;
    private final RouteRepository routeRepository;
    private final ScheduleSeatService scheduleSeatService;

    public TrainScheduleService(
        TrainScheduleRepository trainScheduleRepository,
        TrainRepository trainRepository,
        RouteRepository routeRepository,
        ScheduleSeatService scheduleSeatService
    ) {
        this.trainScheduleRepository = trainScheduleRepository;
        this.trainRepository = trainRepository;
        this.routeRepository = routeRepository;
        this.scheduleSeatService = scheduleSeatService;
    }

    public List<TrainScheduleResponse> findAll() {
        return trainScheduleRepository.findAllByOrderByDepartureTimeAsc()
            .stream()
            .map(TrainScheduleResponse::from)
            .toList();
    }

    public TrainScheduleResponse get(Long scheduleId) {
        return TrainScheduleResponse.from(getSchedule(scheduleId));
    }

    public List<TrainScheduleSearchResponse> search(String from, String to, LocalDate date) {
        String originCode = normalizeCode(from);
        String destinationCode = normalizeCode(to);
        return trainScheduleRepository
            .findByRoute_OriginStation_CodeAndRoute_DestinationStation_CodeAndOperationDateAndStatusInOrderByDepartureTimeAsc(
                originCode,
                destinationCode,
                date,
                BOOKABLE_SEARCH_STATUSES
            )
            .stream()
            .map(TrainScheduleSearchResponse::from)
            .toList();
    }

    @Transactional
    public TrainScheduleResponse create(TrainScheduleCreateRequest request) {
        validateTime(request.operationDate(), request.departureTime(), request.arrivalTime());
        validateTrainTimeConflict(request.trainId(), request.departureTime(), request.arrivalTime());

        Train train = getTrain(request.trainId());
        Route route = getRoute(request.routeId());
        TrainSchedule schedule = TrainSchedule.create(
            train,
            route,
            request.operationDate(),
            request.departureTime(),
            request.arrivalTime()
        );
        TrainSchedule savedSchedule = trainScheduleRepository.save(schedule);
        scheduleSeatService.createAvailableSeatsForSchedule(savedSchedule);
        return TrainScheduleResponse.from(savedSchedule);
    }

    @Transactional
    public TrainScheduleResponse update(Long scheduleId, TrainScheduleUpdateRequest request) {
        TrainSchedule schedule = getSchedule(scheduleId);
        validateTime(request.operationDate(), request.departureTime(), request.arrivalTime());
        validateTrainTimeConflictExceptSelf(
            schedule.getTrain().getId(),
            request.departureTime(),
            request.arrivalTime(),
            scheduleId
        );

        Route route = getRoute(request.routeId());
        schedule.update(route, request.operationDate(), request.departureTime(), request.arrivalTime());
        return TrainScheduleResponse.from(schedule);
    }

    @Transactional
    public TrainScheduleResponse updateStatus(Long scheduleId, TrainScheduleStatusUpdateRequest request) {
        TrainSchedule schedule = getSchedule(scheduleId);
        schedule.updateStatus(request.status());
        return TrainScheduleResponse.from(schedule);
    }

    @Transactional
    public void delete(Long scheduleId) {
        TrainSchedule schedule = getSchedule(scheduleId);
        trainScheduleRepository.delete(schedule);
    }

    private TrainSchedule getSchedule(Long scheduleId) {
        return trainScheduleRepository.findById(scheduleId)
            .orElseThrow(() -> new BusinessException(ErrorCode.SCHEDULE_NOT_FOUND));
    }

    private Train getTrain(Long trainId) {
        return trainRepository.findById(trainId)
            .orElseThrow(() -> new BusinessException(ErrorCode.TRAIN_NOT_FOUND));
    }

    private Route getRoute(Long routeId) {
        return routeRepository.findById(routeId)
            .orElseThrow(() -> new BusinessException(ErrorCode.ROUTE_NOT_FOUND));
    }

    private void validateTime(LocalDate operationDate, LocalDateTime departureTime, LocalDateTime arrivalTime) {
        if (!arrivalTime.isAfter(departureTime)) {
            throw new BusinessException(ErrorCode.INVALID_SCHEDULE_TIME);
        }
        if (!departureTime.toLocalDate().equals(operationDate)) {
            throw new BusinessException(ErrorCode.INVALID_SCHEDULE_TIME);
        }
    }

    private void validateTrainTimeConflict(Long trainId, LocalDateTime departureTime, LocalDateTime arrivalTime) {
        if (trainScheduleRepository.existsByTrain_IdAndDepartureTimeLessThanAndArrivalTimeGreaterThan(
            trainId,
            arrivalTime,
            departureTime
        )) {
            throw new BusinessException(ErrorCode.TRAIN_SCHEDULE_CONFLICT);
        }
    }

    private void validateTrainTimeConflictExceptSelf(
        Long trainId,
        LocalDateTime departureTime,
        LocalDateTime arrivalTime,
        Long scheduleId
    ) {
        if (trainScheduleRepository.existsByTrain_IdAndDepartureTimeLessThanAndArrivalTimeGreaterThanAndIdNot(
            trainId,
            arrivalTime,
            departureTime,
            scheduleId
        )) {
            throw new BusinessException(ErrorCode.TRAIN_SCHEDULE_CONFLICT);
        }
    }

    private String normalizeCode(String code) {
        return code == null ? null : code.trim().toUpperCase();
    }
}