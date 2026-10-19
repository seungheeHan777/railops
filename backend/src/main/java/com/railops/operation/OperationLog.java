package com.railops.operation;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "operation_logs")
public class OperationLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    private Long actorId;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 64) private OperationAction action;
    @Column(nullable = false, length = 32) private String targetType;
    private Long targetId;
    @Column(nullable = false) private LocalDateTime createdAt;
    protected OperationLog() {}
    public OperationLog(Long actorId, OperationAction action, String targetType, Long targetId, LocalDateTime now) {
        this.actorId = actorId; this.action = action; this.targetType = targetType; this.targetId = targetId; this.createdAt = now;
    }
    public Long getId() { return id; }
    public Long getActorId() { return actorId; }
    public OperationAction getAction() { return action; }
    public String getTargetType() { return targetType; }
    public Long getTargetId() { return targetId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
