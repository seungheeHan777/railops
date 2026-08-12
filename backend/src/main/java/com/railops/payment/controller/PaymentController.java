package com.railops.payment.controller;

import com.railops.auth.security.UserPrincipal;
import com.railops.common.response.ApiResponse;
import com.railops.payment.dto.PaymentResultResponse;
import com.railops.payment.service.PaymentService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/{paymentId}/simulate-success")
    public ApiResponse<PaymentResultResponse> simulateSuccess(
        @AuthenticationPrincipal UserPrincipal principal,
        @PathVariable Long paymentId
    ) {
        return ApiResponse.ok(paymentService.simulateSuccess(principal, paymentId));
    }

    @PostMapping("/{paymentId}/simulate-fail")
    public ApiResponse<PaymentResultResponse> simulateFail(
        @AuthenticationPrincipal UserPrincipal principal,
        @PathVariable Long paymentId
    ) {
        return ApiResponse.ok(paymentService.simulateFail(principal, paymentId));
    }

    @PostMapping("/{paymentId}/simulate-cancel")
    public ApiResponse<PaymentResultResponse> simulateCancel(
        @AuthenticationPrincipal UserPrincipal principal,
        @PathVariable Long paymentId
    ) {
        return ApiResponse.ok(paymentService.simulateCancel(principal, paymentId));
    }
}