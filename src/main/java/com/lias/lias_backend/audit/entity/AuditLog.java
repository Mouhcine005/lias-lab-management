package com.lias.lias_backend.audit.entity;

import com.lias.lias_backend.member.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "audit_logs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AuditLog extends BaseEntity {

    // Who performed the action (email or "system" if no auth context)
    @Column(nullable = false)
    private String performedBy;

    // What action (e.g. MEMBER_APPROVED, ROLE_CHANGED, DOCUMENT_UPLOADED)
    @Column(nullable = false)
    private String action;

    // What entity was affected (e.g. Member, Document, Event)
    private String entityType;

    // ID of the affected entity
    private Long entityId;

    // Human-readable detail
    @Column(columnDefinition = "TEXT")
    private String detail;
}
