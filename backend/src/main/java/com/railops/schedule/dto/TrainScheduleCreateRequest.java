package com.railops.schedule.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record TrainScheduleCreateRequest(
    @NotNull(message = "열차 ID는 필수입니다.")
    Long trainId,

    @NotNull(message = "노선 ID는 필수입니다.")
    Long routeId,

    @NotNull(message = "운행일은 필수입니다.")
    LocalDate operationDate,

    @NotNull(message = "출발 시간은 필수입니다.")
    LocalDateTime departureTime,

    @NotNull(message = "도착 시간은 필수입니다.")
    LocalDateTime arrivalTime
) {
}