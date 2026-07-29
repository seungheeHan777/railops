package com.railops.common.response;

public record ApiResponse<T>(
    boolean success,
    T data,
    String message,
    String code
) {

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null, null);
    }

    public static ApiResponse<Void> empty() {
        return new ApiResponse<>(true, null, null, null);
    }

    public static ApiResponse<Void> error(String message, String code) {
        return new ApiResponse<>(false, null, message, code);
    }

    public static <T> ApiResponse<T> error(String message, String code, T data) {
        return new ApiResponse<>(false, data, message, code);
    }
}