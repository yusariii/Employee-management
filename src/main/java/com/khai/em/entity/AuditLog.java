package com.khai.em.entity;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
public class AuditLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate createdAt;

    private String action;

    private String actorUsername;

    private String actorRole;

    private String entityType;

    private Long entityId;

    private String details;

    public AuditLog() {
    }

    public AuditLog(LocalDate createdAt, String action, String actorUsername, String actorRole, String entityType, Long entityId, String details) {
        this.createdAt = createdAt;
        this.action = action;
        this.actorUsername = actorUsername;
        this.actorRole = actorRole;
        this.entityType = entityType;
        this.entityId = entityId;
        this.details = details;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDate.now();
    }
}
