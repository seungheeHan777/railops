package com.railops.train.service;

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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CarService {

    private final CarRepository carRepository;
    private final TrainRepository trainRepository;

    public CarService(CarRepository carRepository, TrainRepository trainRepository) {
        this.carRepository = carRepository;
        this.trainRepository = trainRepository;
    }

    public List<CarResponse> findByTrain(Long trainId) {
        validateTrainExists(trainId);
        return carRepository.findByTrain_IdOrderByCarNoAsc(trainId)
            .stream()
            .map(CarResponse::from)
            .toList();
    }

    public CarResponse get(Long carId) {
        return CarResponse.from(getCar(carId));
    }

    @Transactional
    public CarResponse create(Long trainId, CarCreateRequest request) {
        Train train = getTrain(trainId);
        if (carRepository.existsByTrain_IdAndCarNo(trainId, request.carNo())) {
            throw new BusinessException(ErrorCode.DUPLICATE_CAR_NO);
        }

        Car car = Car.create(train, request.carNo(), request.seatCount());
        return CarResponse.from(carRepository.save(car));
    }

    @Transactional
    public CarResponse update(Long carId, CarUpdateRequest request) {
        Car car = getCar(carId);
        Long trainId = car.getTrain().getId();

        if (carRepository.existsByTrain_IdAndCarNoAndIdNot(trainId, request.carNo(), carId)) {
            throw new BusinessException(ErrorCode.DUPLICATE_CAR_NO);
        }

        car.update(request.carNo(), request.seatCount());
        return CarResponse.from(car);
    }

    @Transactional
    public void delete(Long carId) {
        Car car = getCar(carId);
        carRepository.delete(car);
    }

    private Car getCar(Long carId) {
        return carRepository.findById(carId)
            .orElseThrow(() -> new BusinessException(ErrorCode.CAR_NOT_FOUND));
    }

    private Train getTrain(Long trainId) {
        return trainRepository.findById(trainId)
            .orElseThrow(() -> new BusinessException(ErrorCode.TRAIN_NOT_FOUND));
    }

    private void validateTrainExists(Long trainId) {
        if (!trainRepository.existsById(trainId)) {
            throw new BusinessException(ErrorCode.TRAIN_NOT_FOUND);
        }
    }
}