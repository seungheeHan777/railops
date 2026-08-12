package com.railops.reservation.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record ReservationHoldRequest(
    @NotNull Long scheduleId,
    @NotEmpty List<@NotNull Long> scheduleSeatIds
) {
}