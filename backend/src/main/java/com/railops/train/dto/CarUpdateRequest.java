package com.railops.train.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CarUpdateRequest(
    @NotNull(message = "객차 번호는 필수입니다.")
    @Min(value = 1, message = "객차 번호는 1 이상이어야 합니다.")
    @Max(value = 99, message = "객차 번호는 99 이하여야 합니다.")
    Integer carNo,

    @NotNull(message = "좌석 수는 필수입니다.")
    @Min(value = 1, message = "좌석 수는 1 이상이어야 합니다.")
    @Max(value = 300, message = "좌석 수는 300 이하여야 합니다.")
    Integer seatCount
) {
}