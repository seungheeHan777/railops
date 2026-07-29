package com.railops.station.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record StationCreateRequest(
    @NotBlank(message = "역 이름은 필수입니다.")
    @Size(max = 100, message = "역 이름은 100자 이하여야 합니다.")
    String name,

    @NotBlank(message = "역 코드는 필수입니다.")
    @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "역 코드는 영문, 숫자, _, -만 사용할 수 있습니다.")
    @Size(max = 50, message = "역 코드는 50자 이하여야 합니다.")
    String code,

    @NotBlank(message = "도시는 필수입니다.")
    @Size(max = 100, message = "도시는 100자 이하여야 합니다.")
    String city
) {
}