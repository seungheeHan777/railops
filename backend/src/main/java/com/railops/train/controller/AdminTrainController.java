package com.railops.train.controller;

import com.railops.common.response.ApiResponse;
import com.railops.train.dto.TrainCreateRequest;
import com.railops.train.dto.TrainResponse;
import com.railops.train.dto.TrainUpdateRequest;
import com.railops.train.service.TrainService;
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
@RequestMapping("/api/admin/trains")
public class AdminTrainController {

    private final TrainService trainService;

    public AdminTrainController(TrainService trainService) {
        this.trainService = trainService;
    }

    @PostMapping
    public ApiResponse<TrainResponse> create(@Valid @RequestBody TrainCreateRequest request) {
        return ApiResponse.ok(trainService.create(request));
    }

    @GetMapping
    public ApiResponse<List<TrainResponse>> findAll() {
        return ApiResponse.ok(trainService.findAll());
    }

    @GetMapping("/{trainId}")
    public ApiResponse<TrainResponse> get(@PathVariable Long trainId) {
        return ApiResponse.ok(trainService.get(trainId));
    }

    @PatchMapping("/{trainId}")
    public ApiResponse<TrainResponse> update(
        @PathVariable Long trainId,
        @Valid @RequestBody TrainUpdateRequest request
    ) {
        return ApiResponse.ok(trainService.update(trainId, request));
    }

    @DeleteMapping("/{trainId}")
    public ApiResponse<Void> delete(@PathVariable Long trainId) {
        trainService.delete(trainId);
        return ApiResponse.empty();
    }
}