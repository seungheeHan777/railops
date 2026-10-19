package com.railops.user.dto;
import jakarta.validation.constraints.*;
public record ProfileUpdateRequest(@NotBlank @Size(max = 50) String name) {}
