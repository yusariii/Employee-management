package com.khai.em.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.khai.em.entity.AuditLog;
import com.khai.em.entity.User;
import com.khai.em.repository.AuditLogRepository;
import com.khai.em.security.CurrentUserService;
import com.khai.em.dto.audit.response.AuditLogResponse;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Service
public class AuditLogService {
    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private CurrentUserService currentUserService;

    public void log(String action, String entityType, Long entityId, String details){
        User actorUser = currentUserService.requireCurrentUser();
        String actorUsername = actorUser.getUsername();
        String actorRole = actorUser.getRole().toString();

        AuditLog logEntry = new AuditLog();
        logEntry.setAction(action);
        logEntry.setActorUsername(actorUsername);
        logEntry.setActorRole(actorRole);
        logEntry.setEntityType(entityType);
        logEntry.setEntityId(entityId);
        logEntry.setDetails(details);
        auditLogRepository.save(logEntry);

    }

    public List<AuditLogResponse> getLatestLogs(Integer limit) {
        Pageable pageable = PageRequest.of(0, normalizeLimit(limit));
        return auditLogRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(this::toResponse)
                .getContent();
    }

    public List<AuditLogResponse> getLogsByEntity(String entityType, Long entityId, Integer limit) {
        Pageable pageable = PageRequest.of(0, normalizeLimit(limit));
        return auditLogRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(entityType, entityId, pageable)
                .map(this::toResponse)
                .getContent();
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null) {
            return 100;
        }
        if (limit < 1) {
            return 1;
        }
        return Math.min(limit, 500);
    }

    public AuditLogResponse toResponse(AuditLog log) {
        return new AuditLogResponse(
                log.getId(),
                log.getCreatedAt(),
                log.getAction(),
                log.getActorUsername(),
                log.getActorRole(),
                log.getEntityType(),
                log.getEntityId(),
                log.getDetails()
        );
    }
}
