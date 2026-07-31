package com.railops.schedule.controller;

import com.railops.common.response.ApiResponse;
import com.railops.schedule.dto.TrainScheduleResponse;
import com.railops.schedule.dto.TrainScheduleSearchResponse;
import com.railops.schedule.service.TrainScheduleService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/train-schedules")
public class TrainScheduleController {

    private final TrainScheduleService trainScheduleService;

    public TrainScheduleController(TrainScheduleService trainScheduleService) {
        this.trainScheduleService = trainScheduleService;
    }

    @GetMapping
    public ApiResponse<List<TrainScheduleSearchResponse>> search(
        @RequestParam("from") @NotBlank String from,
        @RequestParam("to") @NotBlank String to,
        @RequestParam("date") @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ApiResponse.ok(trainScheduleService.search(from, to, date));
    }

    @GetMapping("/{scheduleId}")
    public ApiResponse<TrainScheduleResponse> get(@PathVariable Long scheduleId) {
        return ApiResponse.ok(trainScheduleService.get(scheduleId));
    }
}