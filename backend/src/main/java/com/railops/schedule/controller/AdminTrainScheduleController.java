package com.railops.schedule.controller;

import com.railops.common.response.ApiResponse;
import com.railops.schedule.dto.TrainScheduleCreateRequest;
import com.railops.schedule.dto.TrainScheduleResponse;
import com.railops.schedule.dto.TrainScheduleStatusUpdateRequest;
import com.railops.schedule.dto.TrainScheduleUpdateRequest;
import com.railops.schedule.service.TrainScheduleService;
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
@RequestMapping("/api/admin/train-schedules")
public class AdminTrainScheduleController {

    private final TrainScheduleService trainScheduleService;

    public AdminTrainScheduleController(TrainScheduleService trainScheduleService) {
        this.trainScheduleService = trainScheduleService;
    }

    @PostMapping
    public ApiResponse<TrainScheduleResponse> create(@Valid @RequestBody TrainScheduleCreateRequest request) {
        return ApiResponse.ok(trainScheduleService.create(request));
    }

    @GetMapping
    public ApiResponse<List<TrainScheduleResponse>> findAll() {
        return ApiResponse.ok(trainScheduleService.findAll());
    }

    @GetMapping("/{scheduleId}")
    public ApiResponse<TrainScheduleResponse> get(@PathVariable Long scheduleId) {
        return ApiResponse.ok(trainScheduleService.get(scheduleId));
    }

    @PatchMapping("/{scheduleId}")
    public ApiResponse<TrainScheduleResponse> update(
        @PathVariable Long scheduleId,
        @Valid @RequestBody TrainScheduleUpdateRequest request
    ) {
        return ApiResponse.ok(trainScheduleService.update(scheduleId, request));
    }

    @PatchMapping("/{scheduleId}/status")
    public ApiResponse<TrainScheduleResponse> updateStatus(
        @PathVariable Long scheduleId,
        @Valid @RequestBody TrainScheduleStatusUpdateRequest request
    ) {
        return ApiResponse.ok(trainScheduleService.updateStatus(scheduleId, request));
    }

    @DeleteMapping("/{scheduleId}")
    public ApiResponse<Void> delete(@PathVariable Long scheduleId) {
        trainScheduleService.delete(scheduleId);
        return ApiResponse.empty();
    }
}