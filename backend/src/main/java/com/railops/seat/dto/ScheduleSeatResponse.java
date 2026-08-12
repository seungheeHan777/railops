package com.railops.seat.dto;

import com.railops.seat.domain.ScheduleSeat;
import com.railops.seat.domain.ScheduleSeatStatus;
import com.railops.train.domain.SeatType;
import java.time.LocalDateTime;

public record ScheduleSeatResponse(
    Long scheduleSeatId,
    Long seatId,
    String seatNo,
    SeatType seatType,
    ScheduleSeatStatus status,
    LocalDateTime holdExpiresAt
) {

    public static ScheduleSeatResponse from(ScheduleSeat scheduleSeat) {
        return new ScheduleSeatResponse(
            scheduleSeat.getId(),
            scheduleSeat.getSeat().getId(),
            scheduleSeat.getSeat().getSeatNo(),
            scheduleSeat.getSeat().getSeatType(),
            scheduleSeat.getStatus(),
            scheduleSeat.getHoldExpiresAt()
        );
    }
}