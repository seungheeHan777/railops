package com.railops.reservation.controller;

import com.railops.auth.security.UserPrincipal;
import com.railops.common.response.ApiResponse;
import com.railops.reservation.dto.ReservationHoldRequest;
import com.railops.reservation.dto.ReservationHoldResponse;
import com.railops.reservation.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping("/hold")
    public ApiResponse<ReservationHoldResponse> hold(
        @AuthenticationPrincipal UserPrincipal principal,
        @Valid @RequestBody ReservationHoldRequest request
    ) {
        return ApiResponse.ok(reservationService.hold(principal, request));
    }
}