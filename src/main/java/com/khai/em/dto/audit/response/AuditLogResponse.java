package com.khai.em.dto.audit.response;

import java.time.LocalDate;

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getActorUsername() {
        return actorUsername;
    }

    public void setActorUsername(String actorUsername) {
        this.actorUsername = actorUsername;
    }

    public String getActorRole() {
        return actorRole;
    }

    public void setActorRole(String actorRole) {
        this.actorRole = actorRole;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }
}
