package com.railops.reservation.scheduler;

import com.railops.reservation.service.ReservationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class HoldExpirationScheduler {

    private static final Logger log = LoggerFactory.getLogger(HoldExpirationScheduler.class);

    private final ReservationService reservationService;

    public HoldExpirationScheduler(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @Scheduled(fixedDelayString = "${railops.hold.expiration-fixed-delay-ms:60000}")
    public void expireHolds() {
        int affectedRows = reservationService.expireHolds();
        if (affectedRows > 0) {
            log.info("Expired reservation holds: affectedRows={}", affectedRows);
        }
    }
}