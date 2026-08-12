package com.railops.seat.controller;

import com.railops.common.response.ApiResponse;
import com.railops.seat.dto.ScheduleSeatMapResponse;
import com.railops.seat.service.ScheduleSeatService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/train-schedules/{scheduleId}/seats")
public class ScheduleSeatController {

    private final ScheduleSeatService scheduleSeatService;

    public ScheduleSeatController(ScheduleSeatService scheduleSeatService) {
        this.scheduleSeatService = scheduleSeatService;
    }

    @GetMapping
    public ApiResponse<ScheduleSeatMapResponse> getSeatMap(@PathVariable Long scheduleId) {
        return ApiResponse.ok(scheduleSeatService.getSeatMap(scheduleId));
    }
}