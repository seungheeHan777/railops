package com.railops.reservation.controller;

import com.railops.auth.security.UserPrincipal;
import com.railops.common.response.PageResponse;
import com.railops.payment.service.PaymentService;
import com.railops.reservation.domain.ReservationStatus;
import com.railops.reservation.dto.ReservationDetailResponse;
import com.railops.reservation.dto.ReservationSummaryResponse;
import com.railops.reservation.service.ReservationQueryService;
import com.railops.common.response.ApiResponse;
import com.railops.reservation.dto.ReservationHoldRequest;
import com.railops.reservation.dto.ReservationHoldResponse;
import com.railops.reservation.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;
    private final ReservationQueryService queries;
    private final PaymentService payments;

    public ReservationController(ReservationService reservationService, ReservationQueryService queries, PaymentService payments) {
        this.reservationService = reservationService;
        this.queries = queries;
        this.payments = payments;
    }

    @PostMapping("/hold")
    public ApiResponse<ReservationHoldResponse> hold(
        @AuthenticationPrincipal UserPrincipal principal,
        @Valid @RequestBody ReservationHoldRequest request
    ) {
        return ApiResponse.ok(reservationService.hold(principal, request));
    }

    @GetMapping
    public ApiResponse<PageResponse<ReservationSummaryResponse>> list(@AuthenticationPrincipal UserPrincipal principal,
        @RequestParam(required = false) ReservationStatus status,
        @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(queries.list(principal.getId(), status, page, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<ReservationDetailResponse> detail(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        return ApiResponse.ok(queries.getMine(principal, id));
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<ReservationDetailResponse> cancel(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        payments.cancelReservation(principal, id);
        return ApiResponse.ok(queries.getMine(principal, id));
    }
}
