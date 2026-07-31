package com.railops.train.service;

import com.railops.common.error.BusinessException;
import com.railops.common.error.ErrorCode;
import com.railops.train.domain.Car;
import com.railops.train.domain.Seat;
import com.railops.train.dto.SeatCreateRequest;
import com.railops.train.dto.SeatResponse;
import com.railops.train.dto.SeatUpdateRequest;
import com.railops.train.repository.CarRepository;
import com.railops.train.repository.SeatRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class SeatService {

    private final SeatRepository seatRepository;
    private final CarRepository carRepository;

    public SeatService(SeatRepository seatRepository, CarRepository carRepository) {
        this.seatRepository = seatRepository;
        this.carRepository = carRepository;
    }

    public List<SeatResponse> findByCar(Long carId) {
        validateCarExists(carId);
        return seatRepository.findByCar_IdOrderBySeatNoAsc(carId)
            .stream()
            .map(SeatResponse::from)
            .toList();
    }

    public SeatResponse get(Long seatId) {
        return SeatResponse.from(getSeat(seatId));
    }

    @Transactional
    public SeatResponse create(Long carId, SeatCreateRequest request) {
        Car car = getCar(carId);
        String seatNo = normalizeSeatNo(request.seatNo());
        if (seatRepository.existsByCar_IdAndSeatNo(carId, seatNo)) {
            throw new BusinessException(ErrorCode.DUPLICATE_SEAT_NO);
        }

        Seat seat = Seat.create(car, seatNo, request.seatType());
        return SeatResponse.from(seatRepository.save(seat));
    }

    @Transactional
    public SeatResponse update(Long seatId, SeatUpdateRequest request) {
        Seat seat = getSeat(seatId);
        Long carId = seat.getCar().getId();
        String seatNo = normalizeSeatNo(request.seatNo());

        if (seatRepository.existsByCar_IdAndSeatNoAndIdNot(carId, seatNo, seatId)) {
            throw new BusinessException(ErrorCode.DUPLICATE_SEAT_NO);
        }

        seat.update(seatNo, request.seatType());
        return SeatResponse.from(seat);
    }

    @Transactional
    public void delete(Long seatId) {
        Seat seat = getSeat(seatId);
        seatRepository.delete(seat);
    }

    private Seat getSeat(Long seatId) {
        return seatRepository.findById(seatId)
            .orElseThrow(() -> new BusinessException(ErrorCode.SEAT_NOT_FOUND));
    }

    private Car getCar(Long carId) {
        return carRepository.findById(carId)
            .orElseThrow(() -> new BusinessException(ErrorCode.CAR_NOT_FOUND));
    }

    private void validateCarExists(Long carId) {
        if (!carRepository.existsById(carId)) {
            throw new BusinessException(ErrorCode.CAR_NOT_FOUND);
        }
    }

    private String normalizeSeatNo(String seatNo) {
        return seatNo == null ? null : seatNo.trim().toUpperCase();
    }
}