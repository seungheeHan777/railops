package com.railops.seat.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.railops.common.error.BusinessException;
import com.railops.common.error.ErrorCode;
import com.railops.route.domain.Route;
import com.railops.schedule.domain.TrainSchedule;
import com.railops.schedule.repository.TrainScheduleRepository;
import com.railops.seat.domain.ScheduleSeat;
import com.railops.seat.domain.ScheduleSeatStatus;
import com.railops.seat.dto.ScheduleSeatMapResponse;
import com.railops.seat.dto.ScheduleSeatResponse;
import com.railops.seat.repository.ScheduleSeatRepository;
import com.railops.station.domain.Station;
import com.railops.train.domain.Car;
import com.railops.train.domain.Seat;
import com.railops.train.domain.SeatType;
import com.railops.train.domain.Train;
import com.railops.train.repository.SeatRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ScheduleSeatServiceTest {

    @Mock
    private ScheduleSeatRepository scheduleSeatRepository;

    @Mock
    private TrainScheduleRepository trainScheduleRepository;

    @Mock
    private SeatRepository seatRepository;

    private ScheduleSeatService scheduleSeatService;

    @BeforeEach
    void setUp() {
        scheduleSeatService = new ScheduleSeatService(scheduleSeatRepository, trainScheduleRepository, seatRepository);
    }

    @Test
    void createAvailableSeatsForSchedule() {
        Train train = trainWithId(1L);
        TrainSchedule schedule = scheduleWithId(10L, train);
        Seat seatA = seatWithId(100L, carWithId(20L, train, 1), "12A", SeatType.WINDOW);
        Seat seatB = seatWithId(101L, carWithId(20L, train, 1), "12B", SeatType.AISLE);

        when(scheduleSeatRepository.existsByTrainSchedule_Id(10L)).thenReturn(false);
        when(seatRepository.findByTrainIdForScheduleSeatCreation(1L)).thenReturn(List.of(seatA, seatB));

        scheduleSeatService.createAvailableSeatsForSchedule(schedule);

        ArgumentCaptor<Iterable<ScheduleSeat>> captor = ArgumentCaptor.forClass(Iterable.class);
        verify(scheduleSeatRepository).saveAll(captor.capture());
        List<ScheduleSeat> savedSeats = new ArrayList<>();
        captor.getValue().forEach(savedSeats::add);

        assertThat(savedSeats).hasSize(2);
        assertThat(savedSeats).extracting(ScheduleSeat::getStatus)
            .containsExactly(ScheduleSeatStatus.AVAILABLE, ScheduleSeatStatus.AVAILABLE);
    }

    @Test
    void createAvailableSeatsSkipsWhenAlreadyCreated() {
        TrainSchedule schedule = scheduleWithId(10L, trainWithId(1L));
        when(scheduleSeatRepository.existsByTrainSchedule_Id(10L)).thenReturn(true);

        scheduleSeatService.createAvailableSeatsForSchedule(schedule);

        verify(seatRepository, never()).findByTrainIdForScheduleSeatCreation(1L);
    }

    @Test
    void getSeatMapGroupsSeatsByCar() {
        Train train = trainWithId(1L);
        TrainSchedule schedule = scheduleWithId(10L, train);
        Car car1 = carWithId(20L, train, 1);
        Car car2 = carWithId(21L, train, 2);
        ScheduleSeat seatA = scheduleSeatWithId(1000L, schedule, seatWithId(100L, car1, "12A", SeatType.WINDOW));
        ScheduleSeat seatB = scheduleSeatWithId(1001L, schedule, seatWithId(101L, car1, "12B", SeatType.AISLE));
        ScheduleSeat seatC = scheduleSeatWithId(1002L, schedule, seatWithId(102L, car2, "1A", SeatType.STANDARD));

        when(trainScheduleRepository.findById(10L)).thenReturn(Optional.of(schedule));
        when(scheduleSeatRepository.findSeatMapRows(10L)).thenReturn(List.of(seatA, seatB, seatC));

        ScheduleSeatMapResponse response = scheduleSeatService.getSeatMap(10L);

        assertThat(response.scheduleId()).isEqualTo(10L);
        assertThat(response.cars()).hasSize(2);
        assertThat(response.cars().get(0).carNo()).isEqualTo(1);
        assertThat(response.cars().get(0).seats()).hasSize(2);
        assertThat(response.cars().get(1).carNo()).isEqualTo(2);
    }

    @Test
    void getSeatMapRejectsMissingSchedule() {
        when(trainScheduleRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> scheduleSeatService.getSeatMap(10L))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.SCHEDULE_NOT_FOUND);
    }

    @Test
    void blockScheduleSeat() {
        Train train = trainWithId(1L);
        ScheduleSeat scheduleSeat = scheduleSeatWithId(
            1000L,
            scheduleWithId(10L, train),
            seatWithId(100L, carWithId(20L, train, 1), "12A", SeatType.WINDOW)
        );
        when(scheduleSeatRepository.findById(1000L)).thenReturn(Optional.of(scheduleSeat));

        ScheduleSeatResponse response = scheduleSeatService.block(1000L);

        assertThat(response.status()).isEqualTo(ScheduleSeatStatus.BLOCKED);
    }

    @Test
    void unblockScheduleSeat() {
        Train train = trainWithId(1L);
        ScheduleSeat scheduleSeat = scheduleSeatWithId(
            1000L,
            scheduleWithId(10L, train),
            seatWithId(100L, carWithId(20L, train, 1), "12A", SeatType.WINDOW)
        );
        scheduleSeat.block();
        when(scheduleSeatRepository.findById(1000L)).thenReturn(Optional.of(scheduleSeat));

        ScheduleSeatResponse response = scheduleSeatService.unblock(1000L);

        assertThat(response.status()).isEqualTo(ScheduleSeatStatus.AVAILABLE);
    }

    @Test
    void blockRejectsMissingScheduleSeat() {
        when(scheduleSeatRepository.findById(1000L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> scheduleSeatService.block(1000L))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.SCHEDULE_SEAT_NOT_FOUND);
    }

    private Train trainWithId(Long id) {
        Train train = Train.create("KTX-101", "KTX", "KTX 101");
        ReflectionTestUtils.setField(train, "id", id);
        return train;
    }

    private Car carWithId(Long id, Train train, Integer carNo) {
        Car car = Car.create(train, carNo, 56);
        ReflectionTestUtils.setField(car, "id", id);
        return car;
    }

    private Seat seatWithId(Long id, Car car, String seatNo, SeatType seatType) {
        Seat seat = Seat.create(car, seatNo, seatType);
        ReflectionTestUtils.setField(seat, "id", id);
        return seat;
    }

    private TrainSchedule scheduleWithId(Long id, Train train) {
        Station origin = stationWithId(200L, "Seoul", "SEOUL", "Seoul");
        Station destination = stationWithId(201L, "Busan", "BUSAN", "Busan");
        Route route = Route.create("Seoul-Busan", origin, destination);
        ReflectionTestUtils.setField(route, "id", 300L);
        TrainSchedule schedule = TrainSchedule.create(
            train,
            route,
            LocalDate.of(2026, 8, 1),
            LocalDateTime.of(2026, 8, 1, 9, 0),
            LocalDateTime.of(2026, 8, 1, 11, 40)
        );
        ReflectionTestUtils.setField(schedule, "id", id);
        return schedule;
    }

    private Station stationWithId(Long id, String name, String code, String city) {
        Station station = Station.create(name, code, city);
        ReflectionTestUtils.setField(station, "id", id);
        return station;
    }

    private ScheduleSeat scheduleSeatWithId(Long id, TrainSchedule schedule, Seat seat) {
        ScheduleSeat scheduleSeat = ScheduleSeat.createAvailable(schedule, seat);
        ReflectionTestUtils.setField(scheduleSeat, "id", id);
        return scheduleSeat;
    }
}