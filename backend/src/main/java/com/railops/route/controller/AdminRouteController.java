package com.railops.route.controller;

import com.railops.common.response.ApiResponse;
import com.railops.route.dto.RouteCreateRequest;
import com.railops.route.dto.RouteResponse;
import com.railops.route.dto.RouteUpdateRequest;
import com.railops.route.service.RouteService;
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
@RequestMapping("/api/admin/routes")
public class AdminRouteController {

    private final RouteService routeService;

    public AdminRouteController(RouteService routeService) {
        this.routeService = routeService;
    }

    @PostMapping
    public ApiResponse<RouteResponse> create(@Valid @RequestBody RouteCreateRequest request) {
        return ApiResponse.ok(routeService.create(request));
    }

    @GetMapping
    public ApiResponse<List<RouteResponse>> findAll() {
        return ApiResponse.ok(routeService.findAll());
    }

    @GetMapping("/{routeId}")
    public ApiResponse<RouteResponse> get(@PathVariable Long routeId) {
        return ApiResponse.ok(routeService.get(routeId));
    }

    @PatchMapping("/{routeId}")
    public ApiResponse<RouteResponse> update(
        @PathVariable Long routeId,
        @Valid @RequestBody RouteUpdateRequest request
    ) {
        return ApiResponse.ok(routeService.update(routeId, request));
    }

    @DeleteMapping("/{routeId}")
    public ApiResponse<Void> delete(@PathVariable Long routeId) {
        routeService.delete(routeId);
        return ApiResponse.empty();
    }
}