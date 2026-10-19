package com.railops.operation;

import java.time.Clock;
import java.time.LocalDateTime;
import org.springframework.security.core.context.SecurityContextHolder;
import com.railops.auth.security.UserPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;

@Service
public class OperationLogService {
    private final OperationLogRepository logs;
    private final Clock clock;
    public OperationLogService(OperationLogRepository logs, Clock clock) { this.logs = logs; this.clock = clock; }
    @Transactional
    public void record(OperationAction action, String type, Long id) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        Long actorId = authentication != null && authentication.getPrincipal() instanceof UserPrincipal p ? p.getId() : null;
        logs.save(new OperationLog(actorId, action, type, id, LocalDateTime.now(clock)));
    }
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void loginFailed() { logs.save(new OperationLog(null, OperationAction.LOGIN_FAILED, "AUTH", null, LocalDateTime.now(clock))); }
}
