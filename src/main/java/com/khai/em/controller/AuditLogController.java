package com.khai.em.controller;

import java.util.List;

import jakarta.validation.constraints.Positive;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.validation.annotation.Validated;

import com.khai.em.dto.audit.response.AuditLogResponse;
import com.khai.em.service.AuditLogService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/audit-logs")
@CrossOrigin(origins = "*")
@Validated
@RequiredArgsConstructor
public class AuditLogController {
    private final AuditLogService auditLogService;

    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @GetMapping
    public ResponseEntity<List<AuditLogResponse>> getAuditLogs(
        @RequestParam(name = "limit", required = false) @Positive(message = "limit must be positive") Integer limit,
        @RequestParam(name = "entityType", required = false) String entityType,
        @RequestParam(name = "entityId", required = false) @Positive(message = "entityId must be positive") Long entityId
    ) {

        if (entityType != null && entityId != null) {
            return ResponseEntity.ok(auditLogService.getLogsByEntity(entityType, entityId, limit));
        }
        return ResponseEntity.ok(auditLogService.getLatestLogs(limit));
    }
}
