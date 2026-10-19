package com.railops.reservation.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;

public record ReservationHoldRequest(
    @NotNull @Positive Long scheduleId,
    @NotEmpty @Size(max = 10) List<@NotNull @Positive Long> scheduleSeatIds
) {
}
