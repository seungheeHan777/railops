package com.railops.route.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RouteCreateRequest(
    @NotBlank(message = "노선 이름은 필수입니다.")
    @Size(max = 100, message = "노선 이름은 100자 이하여야 합니다.")
    String name,

    @NotNull(message = "출발역 ID는 필수입니다.")
    Long originStationId,

    @NotNull(message = "도착역 ID는 필수입니다.")
    Long destinationStationId
) {
}