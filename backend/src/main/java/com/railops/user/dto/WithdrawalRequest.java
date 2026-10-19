package com.railops.user.dto;
import jakarta.validation.constraints.*;
public record WithdrawalRequest(@NotBlank @Size(max = 72) String currentPassword) {}
