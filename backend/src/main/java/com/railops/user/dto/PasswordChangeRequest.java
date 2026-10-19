package com.railops.user.dto;
import jakarta.validation.constraints.*;
public record PasswordChangeRequest(@NotBlank @Size(max = 72) String currentPassword,
    @NotBlank @Size(min = 8, max = 72) String newPassword) {}
