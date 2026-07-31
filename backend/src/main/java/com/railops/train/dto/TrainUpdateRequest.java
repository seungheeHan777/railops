package com.railops.train.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record TrainUpdateRequest(
    @NotBlank(message = "열차 번호는 필수입니다.")
    @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "열차 번호는 영문, 숫자, _, -만 사용할 수 있습니다.")
    @Size(max = 50, message = "열차 번호는 50자 이하여야 합니다.")
    String trainNo,

    @NotBlank(message = "열차 타입은 필수입니다.")
    @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "열차 타입은 영문, 숫자, _, -만 사용할 수 있습니다.")
    @Size(max = 50, message = "열차 타입은 50자 이하여야 합니다.")
    String trainType,

    @NotBlank(message = "열차 이름은 필수입니다.")
    @Size(max = 100, message = "열차 이름은 100자 이하여야 합니다.")
    String name
) {
}