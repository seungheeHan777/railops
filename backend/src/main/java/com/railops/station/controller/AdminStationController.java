package com.railops.station.controller;

import com.railops.common.response.ApiResponse;
import com.railops.station.dto.StationCreateRequest;
import com.railops.station.dto.StationResponse;
import com.railops.station.dto.StationUpdateRequest;
import com.railops.station.service.StationService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/stations")
public class AdminStationController {

    private final StationService stationService;

    public AdminStationController(StationService stationService) {
        this.stationService = stationService;
    }

    @PostMapping
    public ApiResponse<StationResponse> create(@Valid @RequestBody StationCreateRequest request) {
        return ApiResponse.ok(stationService.create(request));
    }

    @GetMapping
    public ApiResponse<List<StationResponse>> findAll() {
        return ApiResponse.ok(stationService.findAll());
    }

    @GetMapping("/{stationId}")
    public ApiResponse<StationResponse> get(@PathVariable Long stationId) {
        return ApiResponse.ok(stationService.get(stationId));
    }

    @PatchMapping("/{stationId}")
    public ApiResponse<StationResponse> update(
        @PathVariable Long stationId,
        @Valid @RequestBody StationUpdateRequest request
    ) {
        return ApiResponse.ok(stationService.update(stationId, request));
    }

    @DeleteMapping("/{stationId}")
    public ApiResponse<Void> delete(@PathVariable Long stationId) {
        stationService.delete(stationId);
        return ApiResponse.empty();
    }
}