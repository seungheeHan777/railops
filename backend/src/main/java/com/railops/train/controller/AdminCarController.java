package com.railops.train.controller;

import com.railops.common.response.ApiResponse;
import com.railops.train.dto.CarCreateRequest;
import com.railops.train.dto.CarResponse;
import com.railops.train.dto.CarUpdateRequest;
import com.railops.train.service.CarService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdminCarController {

    private final CarService carService;

    public AdminCarController(CarService carService) {
        this.carService = carService;
    }

    @PostMapping("/api/admin/trains/{trainId}/cars")
    public ApiResponse<CarResponse> create(
        @PathVariable Long trainId,
        @Valid @RequestBody CarCreateRequest request
    ) {
        return ApiResponse.ok(carService.create(trainId, request));
    }

    @GetMapping("/api/admin/trains/{trainId}/cars")
    public ApiResponse<List<CarResponse>> findByTrain(@PathVariable Long trainId) {
        return ApiResponse.ok(carService.findByTrain(trainId));
    }

    @GetMapping("/api/admin/cars/{carId}")
    public ApiResponse<CarResponse> get(@PathVariable Long carId) {
        return ApiResponse.ok(carService.get(carId));
    }

    @PatchMapping("/api/admin/cars/{carId}")
    public ApiResponse<CarResponse> update(
        @PathVariable Long carId,
        @Valid @RequestBody CarUpdateRequest request
    ) {
        return ApiResponse.ok(carService.update(carId, request));
    }

    @DeleteMapping("/api/admin/cars/{carId}")
    public ApiResponse<Void> delete(@PathVariable Long carId) {
        carService.delete(carId);
        return ApiResponse.empty();
    }
}