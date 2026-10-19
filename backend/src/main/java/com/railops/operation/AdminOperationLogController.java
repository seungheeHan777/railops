package com.railops.operation;

import com.railops.common.error.*;
import com.railops.common.response.*;
import java.time.*;
import org.springframework.data.domain.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/operation-logs")
public class AdminOperationLogController {
    private final OperationLogRepository logs;
    private final Clock clock;
    public AdminOperationLogController(OperationLogRepository logs, Clock clock) { this.logs = logs; this.clock = clock; }
    @GetMapping @Transactional(readOnly = true)
    public ApiResponse<PageResponse<LogResponse>> list(@RequestParam(required = false) OperationAction action,
        @RequestParam(required = false) LocalDate from, @RequestParam(required = false) LocalDate to,
        @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        LocalDate last = to == null ? LocalDate.now(clock) : to;
        LocalDate first = from == null ? last.minusDays(30) : from;
        if (page < 0 || size < 1 || size > 100 || first.isAfter(last) || last.equals(LocalDate.MAX)) throw new BusinessException(ErrorCode.INVALID_REQUEST);
        return ApiResponse.ok(PageResponse.from(logs.search(action, first.atStartOfDay(), last.plusDays(1).atStartOfDay(),
            PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt", "id"))).map(LogResponse::from)));
    }
    public record LogResponse(Long id, Long actorId, OperationAction action, String targetType, Long targetId, LocalDateTime createdAt) {
        static LogResponse from(OperationLog l) { return new LogResponse(l.getId(), l.getActorId(), l.getAction(), l.getTargetType(), l.getTargetId(), l.getCreatedAt()); }
    }
}
