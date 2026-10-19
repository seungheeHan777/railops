package com.railops.user.controller;

import com.railops.auth.dto.UserSummaryResponse;
import com.railops.auth.security.UserPrincipal;
import com.railops.common.response.ApiResponse;
import com.railops.user.dto.*;
import com.railops.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/users/me")
public class UserController {
    private final UserService users;
    public UserController(UserService users) { this.users = users; }
    @PatchMapping public ApiResponse<UserSummaryResponse> update(@AuthenticationPrincipal UserPrincipal p, @Valid @RequestBody ProfileUpdateRequest r) { return ApiResponse.ok(users.update(p, r)); }
    @PostMapping("/password") public ApiResponse<Void> password(@AuthenticationPrincipal UserPrincipal p, @Valid @RequestBody PasswordChangeRequest r) { users.password(p, r); return ApiResponse.empty(); }
    @PostMapping("/withdraw") public ApiResponse<Void> withdraw(@AuthenticationPrincipal UserPrincipal p, @Valid @RequestBody WithdrawalRequest r) { users.withdraw(p, r); return ApiResponse.empty(); }
}
