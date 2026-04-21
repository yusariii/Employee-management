package com.khai.em.dto.audit.response;

import java.time.LocalDate;

import lombok.Data;

@Data
public class AuditLogResponse {
    private Long id;
    private LocalDate createdAt;
    private String action;
    private String actorUsername;
    private String actorRole;
    private String entityType;
    private Long entityId;
    private String details;

    public AuditLogResponse() {
    }

    public AuditLogResponse(Long id, LocalDate createdAt, String action, String actorUsername, String actorRole, String entityType, Long entityId, String details) {
        this.id = id;
        this.createdAt = createdAt;
        this.action = action;
        this.actorUsername = actorUsername;
        this.actorRole = actorRole;
        this.entityType = entityType;
        this.entityId = entityId;
        this.details = details;
    }
}
