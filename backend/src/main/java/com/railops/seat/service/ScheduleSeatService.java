package com.railops.seat.service;

import com.railops.common.error.BusinessException;
import com.railops.common.error.ErrorCode;
import com.railops.schedule.domain.TrainSchedule;
import com.railops.schedule.repository.TrainScheduleRepository;
import com.railops.seat.domain.ScheduleSeat;
import com.railops.seat.dto.CarSeatResponse;
import com.railops.seat.dto.ScheduleSeatMapResponse;
import com.railops.seat.dto.ScheduleSeatResponse;
import com.railops.seat.repository.ScheduleSeatRepository;
import com.railops.train.domain.Car;
import com.railops.train.domain.Seat;
import com.railops.train.repository.SeatRepository;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ScheduleSeatService {

    private final ScheduleSeatRepository scheduleSeatRepository;
    private final TrainScheduleRepository trainScheduleRepository;
    private final SeatRepository seatRepository;

    public ScheduleSeatService(
        ScheduleSeatRepository scheduleSeatRepository,
        TrainScheduleRepository trainScheduleRepository,
        SeatRepository seatRepository
    ) {
        this.scheduleSeatRepository = scheduleSeatRepository;
        this.trainScheduleRepository = trainScheduleRepository;
        this.seatRepository = seatRepository;
    }

    public ScheduleSeatMapResponse getSeatMap(Long scheduleId) {
        TrainSchedule schedule = getSchedule(scheduleId);
        List<ScheduleSeat> scheduleSeats = scheduleSeatRepository.findSeatMapRows(scheduleId);
        Map<Long, List<ScheduleSeat>> groupedByCar = scheduleSeats.stream()
            .collect(Collectors.groupingBy(
                scheduleSeat -> scheduleSeat.getSeat().getCar().getId(),
                LinkedHashMap::new,
                Collectors.toList()
            ));

        List<CarSeatResponse> cars = groupedByCar.values()
            .stream()
            .map(this::toCarSeatResponse)
            .toList();

        return new ScheduleSeatMapResponse(schedule.getId(), cars);
    }

    @Transactional
    public void createAvailableSeatsForSchedule(TrainSchedule schedule) {
        if (schedule.getId() != null && scheduleSeatRepository.existsByTrainSchedule_Id(schedule.getId())) {
            return;
        }

        List<Seat> seats = seatRepository.findByTrainIdForScheduleSeatCreation(schedule.getTrain().getId());
        List<ScheduleSeat> scheduleSeats = seats.stream()
            .map(seat -> ScheduleSeat.createAvailable(schedule, seat))
            .toList();
        scheduleSeatRepository.saveAll(scheduleSeats);
    }

    @Transactional
    public ScheduleSeatResponse block(Long scheduleSeatId) {
        ScheduleSeat scheduleSeat = getScheduleSeat(scheduleSeatId);
        scheduleSeat.block();
        return ScheduleSeatResponse.from(scheduleSeat);
    }

    @Transactional
    public ScheduleSeatResponse unblock(Long scheduleSeatId) {
        ScheduleSeat scheduleSeat = getScheduleSeat(scheduleSeatId);
        scheduleSeat.unblock();
        return ScheduleSeatResponse.from(scheduleSeat);
    }

    private TrainSchedule getSchedule(Long scheduleId) {
        return trainScheduleRepository.findById(scheduleId)
            .orElseThrow(() -> new BusinessException(ErrorCode.SCHEDULE_NOT_FOUND));
    }

    private ScheduleSeat getScheduleSeat(Long scheduleSeatId) {
        return scheduleSeatRepository.findById(scheduleSeatId)
            .orElseThrow(() -> new BusinessException(ErrorCode.SCHEDULE_SEAT_NOT_FOUND));
    }

    private CarSeatResponse toCarSeatResponse(List<ScheduleSeat> scheduleSeats) {
        ScheduleSeat first = scheduleSeats.get(0);
        Car car = first.getSeat().getCar();
        return new CarSeatResponse(
            car.getId(),
            car.getCarNo(),
            scheduleSeats.stream().map(ScheduleSeatResponse::from).toList()
        );
    }
}