package com.lias.lias_backend.audit.repository;

import com.lias.lias_backend.audit.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByEntityTypeAndEntityId(String entityType, Long entityId);
    List<AuditLog> findByPerformedBy(String email);
    List<AuditLog> findAllByOrderByCreatedAtDesc();
}
