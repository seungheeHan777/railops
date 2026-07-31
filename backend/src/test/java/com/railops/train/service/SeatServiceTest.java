package com.railops.train.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.railops.common.error.BusinessException;
import com.railops.common.error.ErrorCode;
import com.railops.train.domain.Car;
import com.railops.train.domain.Seat;
import com.railops.train.domain.SeatType;
import com.railops.train.domain.Train;
import com.railops.train.dto.SeatCreateRequest;
import com.railops.train.dto.SeatResponse;
import com.railops.train.dto.SeatUpdateRequest;
import com.railops.train.repository.CarRepository;
import com.railops.train.repository.SeatRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class SeatServiceTest {

    @Mock
    private SeatRepository seatRepository;

    @Mock
    private CarRepository carRepository;

    private SeatService seatService;

    @BeforeEach
    void setUp() {
        seatService = new SeatService(seatRepository, carRepository);
    }

    @Test
    void createSeat() {
        Car car = carWithId(10L);
        SeatCreateRequest request = new SeatCreateRequest("12a", SeatType.WINDOW);
        Seat seat = seatWithId(100L, car, "12A", SeatType.WINDOW);

        when(carRepository.findById(10L)).thenReturn(Optional.of(car));
        when(seatRepository.existsByCar_IdAndSeatNo(10L, "12A")).thenReturn(false);
        when(seatRepository.save(any(Seat.class))).thenReturn(seat);

        SeatResponse response = seatService.create(10L, request);

        assertThat(response.carId()).isEqualTo(10L);
        assertThat(response.seatNo()).isEqualTo("12A");
        assertThat(response.seatType()).isEqualTo(SeatType.WINDOW);
    }

    @Test
    void createSeatRejectsMissingCar() {
        SeatCreateRequest request = new SeatCreateRequest("12A", SeatType.WINDOW);
        when(carRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> seatService.create(10L, request))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.CAR_NOT_FOUND);
    }

    @Test
    void createSeatRejectsDuplicateSeatNo() {
        Car car = carWithId(10L);
        SeatCreateRequest request = new SeatCreateRequest("12A", SeatType.WINDOW);

        when(carRepository.findById(10L)).thenReturn(Optional.of(car));
        when(seatRepository.existsByCar_IdAndSeatNo(10L, "12A")).thenReturn(true);

        assertThatThrownBy(() -> seatService.create(10L, request))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.DUPLICATE_SEAT_NO);
    }

    @Test
    void findSeatsByCar() {
        Car car = carWithId(10L);
        when(carRepository.existsById(10L)).thenReturn(true);
        when(seatRepository.findByCar_IdOrderBySeatNoAsc(10L))
            .thenReturn(List.of(seatWithId(100L, car, "12A", SeatType.WINDOW)));

        List<SeatResponse> responses = seatService.findByCar(10L);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).seatNo()).isEqualTo("12A");
    }

    @Test
    void getRejectsMissingSeat() {
        when(seatRepository.findById(100L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> seatService.get(100L))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.SEAT_NOT_FOUND);
    }

    @Test
    void updateSeat() {
        Car car = carWithId(10L);
        Seat seat = seatWithId(100L, car, "12A", SeatType.WINDOW);
        SeatUpdateRequest request = new SeatUpdateRequest("12b", SeatType.AISLE);

        when(seatRepository.findById(100L)).thenReturn(Optional.of(seat));
        when(seatRepository.existsByCar_IdAndSeatNoAndIdNot(10L, "12B", 100L)).thenReturn(false);

        SeatResponse response = seatService.update(100L, request);

        assertThat(response.seatNo()).isEqualTo("12B");
        assertThat(response.seatType()).isEqualTo(SeatType.AISLE);
    }

    @Test
    void updateSeatRejectsDuplicateSeatNo() {
        Car car = carWithId(10L);
        Seat seat = seatWithId(100L, car, "12A", SeatType.WINDOW);
        SeatUpdateRequest request = new SeatUpdateRequest("12B", SeatType.AISLE);

        when(seatRepository.findById(100L)).thenReturn(Optional.of(seat));
        when(seatRepository.existsByCar_IdAndSeatNoAndIdNot(10L, "12B", 100L)).thenReturn(true);

        assertThatThrownBy(() -> seatService.update(100L, request))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.DUPLICATE_SEAT_NO);
    }

    @Test
    void deleteSeat() {
        Car car = carWithId(10L);
        Seat seat = seatWithId(100L, car, "12A", SeatType.WINDOW);
        when(seatRepository.findById(100L)).thenReturn(Optional.of(seat));

        seatService.delete(100L);

        verify(seatRepository).delete(seat);
    }

    private Car carWithId(Long id) {
        Train train = Train.create("KTX-101", "KTX", "KTX 101");
        ReflectionTestUtils.setField(train, "id", 1L);
        Car car = Car.create(train, 1, 56);
        ReflectionTestUtils.setField(car, "id", id);
        return car;
    }

    private Seat seatWithId(Long id, Car car, String seatNo, SeatType seatType) {
        Seat seat = Seat.create(car, seatNo, seatType);
        ReflectionTestUtils.setField(seat, "id", id);
        return seat;
    }
}