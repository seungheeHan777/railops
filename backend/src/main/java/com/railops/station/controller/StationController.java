package com.railops.station.controller;

import com.railops.common.response.ApiResponse;
import com.railops.station.dto.StationResponse;
import com.railops.station.service.StationService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stations")
public class StationController {

    private final StationService stationService;

    public StationController(StationService stationService) {
        this.stationService = stationService;
    }

    @GetMapping
    public ApiResponse<List<StationResponse>> findAll() {
        return ApiResponse.ok(stationService.findAll());
    }

    @GetMapping("/search")
    public ApiResponse<List<StationResponse>> search(@RequestParam(required = false) String keyword) {
        return ApiResponse.ok(stationService.search(keyword));
    }
}