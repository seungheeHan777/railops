package com.railops.train.dto;

import com.railops.train.domain.SeatType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SeatCreateRequest(
    @NotBlank(message = "좌석 번호는 필수입니다.")
    @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "좌석 번호는 영문, 숫자, _, -만 사용할 수 있습니다.")
    @Size(max = 20, message = "좌석 번호는 20자 이하여야 합니다.")
    String seatNo,

    @NotNull(message = "좌석 타입은 필수입니다.")
    SeatType seatType
) {
}