package com.railops.auth.dto;

import com.railops.user.domain.User;
import com.railops.user.domain.UserRole;

public record UserSummaryResponse(
    Long id,
    String email,
    String name,
    UserRole role
) {

    public static UserSummaryResponse from(User user) {
        return new UserSummaryResponse(user.getId(), user.getEmail(), user.getName(), user.getRole());
    }
}