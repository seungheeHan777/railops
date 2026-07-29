package com.railops.auth.dto;

public record LoginResponse(
    String accessToken,
    String tokenType,
    UserSummaryResponse user
) {

    public static LoginResponse bearer(String accessToken, UserSummaryResponse user) {
        return new LoginResponse(accessToken, "Bearer", user);
    }
}