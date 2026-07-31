package com.railops.schedule.dto;

import com.railops.schedule.domain.TrainScheduleStatus;
import jakarta.validation.constraints.NotNull;

public record TrainScheduleStatusUpdateRequest(
    @NotNull(message = "운행편 상태는 필수입니다.")
    TrainScheduleStatus status
) {
}