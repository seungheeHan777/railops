package com.railops.seat.controller;

import com.railops.common.response.ApiResponse;
import com.railops.seat.dto.ScheduleSeatResponse;
import com.railops.seat.service.ScheduleSeatService;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/schedule-seats")
public class AdminScheduleSeatController {

    private final ScheduleSeatService scheduleSeatService;

    public AdminScheduleSeatController(ScheduleSeatService scheduleSeatService) {
        this.scheduleSeatService = scheduleSeatService;
    }

    @PatchMapping("/{scheduleSeatId}/block")
    public ApiResponse<ScheduleSeatResponse> block(@PathVariable Long scheduleSeatId) {
        return ApiResponse.ok(scheduleSeatService.block(scheduleSeatId));
    }

    @PatchMapping("/{scheduleSeatId}/unblock")
    public ApiResponse<ScheduleSeatResponse> unblock(@PathVariable Long scheduleSeatId) {
        return ApiResponse.ok(scheduleSeatService.unblock(scheduleSeatId));
    }
}