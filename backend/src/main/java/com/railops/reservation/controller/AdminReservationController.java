package com.railops.reservation.controller;

import com.railops.common.response.ApiResponse;
import com.railops.common.response.PageResponse;
import com.railops.reservation.domain.ReservationStatus;
import com.railops.reservation.dto.ReservationDetailResponse;
import com.railops.reservation.dto.ReservationSummaryResponse;
import com.railops.reservation.service.ReservationQueryService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/reservations")
public class AdminReservationController {
    private final ReservationQueryService queries;
    public AdminReservationController(ReservationQueryService queries) { this.queries = queries; }

    @GetMapping
    public ApiResponse<PageResponse<ReservationSummaryResponse>> list(
        @RequestParam(required = false) ReservationStatus status,
        @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(queries.list(null, status, page, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<ReservationDetailResponse> detail(@PathVariable Long id) {
        return ApiResponse.ok(queries.detail(null, id));
    }
}
