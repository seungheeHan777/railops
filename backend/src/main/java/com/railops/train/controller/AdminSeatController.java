package com.railops.train.controller;

import com.railops.common.response.ApiResponse;
import com.railops.train.dto.SeatCreateRequest;
import com.railops.train.dto.SeatResponse;
import com.railops.train.dto.SeatUpdateRequest;
import com.railops.train.service.SeatService;
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
public class AdminSeatController {

    private final SeatService seatService;

    public AdminSeatController(SeatService seatService) {
        this.seatService = seatService;
    }

    @PostMapping("/api/admin/cars/{carId}/seats")
    public ApiResponse<SeatResponse> create(
        @PathVariable Long carId,
        @Valid @RequestBody SeatCreateRequest request
    ) {
        return ApiResponse.ok(seatService.create(carId, request));
    }

    @GetMapping("/api/admin/cars/{carId}/seats")
    public ApiResponse<List<SeatResponse>> findByCar(@PathVariable Long carId) {
        return ApiResponse.ok(seatService.findByCar(carId));
    }

    @GetMapping("/api/admin/seats/{seatId}")
    public ApiResponse<SeatResponse> get(@PathVariable Long seatId) {
        return ApiResponse.ok(seatService.get(seatId));
    }

    @PatchMapping("/api/admin/seats/{seatId}")
    public ApiResponse<SeatResponse> update(
        @PathVariable Long seatId,
        @Valid @RequestBody SeatUpdateRequest request
    ) {
        return ApiResponse.ok(seatService.update(seatId, request));
    }

    @DeleteMapping("/api/admin/seats/{seatId}")
    public ApiResponse<Void> delete(@PathVariable Long seatId) {
        seatService.delete(seatId);
        return ApiResponse.empty();
    }
}