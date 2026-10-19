package com.railops.reservation.scheduler;

import com.railops.payment.domain.PaymentStatus;
import com.railops.payment.repository.PaymentRepository;
import com.railops.payment.service.PaymentService;
import java.time.Clock;
import java.time.LocalDateTime;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "railops.hold.scheduler-enabled", havingValue = "true", matchIfMissing = true)
public class HoldExpirationScheduler {

    private static final Logger log = LoggerFactory.getLogger(HoldExpirationScheduler.class);

    private final PaymentRepository payments;
    private final PaymentService paymentService;
    private final Clock clock;

    public HoldExpirationScheduler(PaymentRepository payments, PaymentService paymentService, Clock clock) {
        this.payments = payments;
        this.paymentService = paymentService;
        this.clock = clock;
    }

    @Scheduled(fixedDelayString = "${railops.hold.expiration-fixed-delay-ms:60000}")
    public void expireHolds() {
        int affectedRows = 0;
        var candidates = payments.findExpiredCandidateIds(PaymentStatus.READY, LocalDateTime.now(clock), PageRequest.of(0, 100));
        for (Long id : candidates) {
            try {
                if (paymentService.expireIfDue(id)) affectedRows++;
            } catch (RuntimeException exception) {
                log.error("HOLD expiration failed: paymentId={}", id, exception);
            }
        }
        if (affectedRows > 0) {
            log.info("Expired reservation holds: affectedRows={}", affectedRows);
        }
    }
}
