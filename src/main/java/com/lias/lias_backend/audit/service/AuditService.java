package com.lias.lias_backend.audit.service;

import com.lias.lias_backend.audit.entity.AuditLog;
import com.lias.lias_backend.audit.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    /**
     * Call this from any service where you want to log an important action.
     * Examples:
     *   auditService.log("MEMBER_APPROVED", "Member", member.getId(), "Member John Doe approved");
     *   auditService.log("DOCUMENT_UPLOADED", "Document", doc.getId(), "Uploaded: Report2025.pdf");
     *   auditService.log("ROLE_CHANGED", "User", user.getId(), "Role changed to DIRECTOR");
     */
    public void log(String action, String entityType, Long entityId, String detail) {
        String actor = "system";
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && auth.getName() != null) {
                actor = auth.getName();
            }
        } catch (Exception ignored) {}

        AuditLog log = AuditLog.builder()
                .performedBy(actor)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .detail(detail)
                .build();

        auditLogRepository.save(log);
    }

    public List<AuditLog> getAll() {
        return auditLogRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<AuditLog> getForEntity(String entityType, Long entityId) {
        return auditLogRepository.findByEntityTypeAndEntityId(entityType, entityId);
    }

    public List<AuditLog> getByActor(String email) {
        return auditLogRepository.findByPerformedBy(email);
    }
}
