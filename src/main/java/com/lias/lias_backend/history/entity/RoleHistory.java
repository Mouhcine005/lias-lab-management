package com.lias.lias_backend.history.entity;

import com.lias.lias_backend.member.entity.BaseEntity;
import com.lias.lias_backend.member.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

// Timeline of a user's role assignments (spec §7 — "Historique complet des rôles").
// The current role stays cached on User.role for cheap reads/authorization checks;
// this table is the source of truth for "who had what role, and when".
@Entity
@Table(name = "role_history")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RoleHistory extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private User.UserRole role;

    @Column(nullable = false)
    private LocalDate startDate;

    private LocalDate endDate;

    // Email of the admin who made the change ("system" on initial registration)
    private String changedBy;

    public boolean isActive() {
        return endDate == null;
    }
}
