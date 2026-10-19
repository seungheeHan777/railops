package com.railops.operation;

import java.time.LocalDateTime;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface OperationLogRepository extends JpaRepository<OperationLog, Long> {
    @Query("select l from OperationLog l where (:action is null or l.action = :action) and l.createdAt >= :from and l.createdAt < :to")
    Page<OperationLog> search(@Param("action") OperationAction action, @Param("from") LocalDateTime from,
        @Param("to") LocalDateTime to, Pageable pageable);
}
