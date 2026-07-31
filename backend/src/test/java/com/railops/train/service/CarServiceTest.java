package com.railops.train.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.railops.common.error.BusinessException;
import com.railops.common.error.ErrorCode;
import com.railops.train.domain.Car;
import com.railops.train.domain.Train;
import com.railops.train.dto.CarCreateRequest;
import com.railops.train.dto.CarResponse;
import com.railops.train.dto.CarUpdateRequest;
import com.railops.train.repository.CarRepository;
import com.railops.train.repository.TrainRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class CarServiceTest {

    @Mock
    private CarRepository carRepository;

    @Mock
    private TrainRepository trainRepository;

    private CarService carService;

    @BeforeEach
    void setUp() {
        carService = new CarService(carRepository, trainRepository);
    }

    @Test
    void createCar() {
        Train train = trainWithId(1L);
        CarCreateRequest request = new CarCreateRequest(1, 56);
        Car car = carWithId(10L, train, 1, 56);

        when(trainRepository.findById(1L)).thenReturn(Optional.of(train));
        when(carRepository.existsByTrain_IdAndCarNo(1L, 1)).thenReturn(false);
        when(carRepository.save(any(Car.class))).thenReturn(car);

        CarResponse response = carService.create(1L, request);

        assertThat(response.trainId()).isEqualTo(1L);
        assertThat(response.carNo()).isEqualTo(1);
        assertThat(response.seatCount()).isEqualTo(56);
    }

    @Test
    void createCarRejectsMissingTrain() {
        CarCreateRequest request = new CarCreateRequest(1, 56);
        when(trainRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> carService.create(1L, request))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.TRAIN_NOT_FOUND);
    }

    @Test
    void createCarRejectsDuplicateCarNo() {
        Train train = trainWithId(1L);
        CarCreateRequest request = new CarCreateRequest(1, 56);

        when(trainRepository.findById(1L)).thenReturn(Optional.of(train));
        when(carRepository.existsByTrain_IdAndCarNo(1L, 1)).thenReturn(true);

        assertThatThrownBy(() -> carService.create(1L, request))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.DUPLICATE_CAR_NO);
    }

    @Test
    void findCarsByTrain() {
        Train train = trainWithId(1L);
        when(trainRepository.existsById(1L)).thenReturn(true);
        when(carRepository.findByTrain_IdOrderByCarNoAsc(1L)).thenReturn(List.of(carWithId(10L, train, 1, 56)));

        List<CarResponse> responses = carService.findByTrain(1L);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).carNo()).isEqualTo(1);
    }

    @Test
    void getRejectsMissingCar() {
        when(carRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> carService.get(1L))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.CAR_NOT_FOUND);
    }

    @Test
    void updateCar() {
        Train train = trainWithId(1L);
        Car car = carWithId(10L, train, 1, 56);
        CarUpdateRequest request = new CarUpdateRequest(2, 40);

        when(carRepository.findById(10L)).thenReturn(Optional.of(car));
        when(carRepository.existsByTrain_IdAndCarNoAndIdNot(1L, 2, 10L)).thenReturn(false);

        CarResponse response = carService.update(10L, request);

        assertThat(response.carNo()).isEqualTo(2);
        assertThat(response.seatCount()).isEqualTo(40);
    }

    @Test
    void updateCarRejectsDuplicateCarNo() {
        Train train = trainWithId(1L);
        Car car = carWithId(10L, train, 1, 56);
        CarUpdateRequest request = new CarUpdateRequest(2, 40);

        when(carRepository.findById(10L)).thenReturn(Optional.of(car));
        when(carRepository.existsByTrain_IdAndCarNoAndIdNot(1L, 2, 10L)).thenReturn(true);

        assertThatThrownBy(() -> carService.update(10L, request))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.DUPLICATE_CAR_NO);
    }

    @Test
    void deleteCar() {
        Train train = trainWithId(1L);
        Car car = carWithId(10L, train, 1, 56);
        when(carRepository.findById(10L)).thenReturn(Optional.of(car));

        carService.delete(10L);

        verify(carRepository).delete(car);
    }

    private Train trainWithId(Long id) {
        Train train = Train.create("KTX-101", "KTX", "KTX 101");
        ReflectionTestUtils.setField(train, "id", id);
        return train;
    }

    private Car carWithId(Long id, Train train, Integer carNo, Integer seatCount) {
        Car car = Car.create(train, carNo, seatCount);
        ReflectionTestUtils.setField(car, "id", id);
        return car;
    }
}