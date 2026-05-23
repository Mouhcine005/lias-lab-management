package com.lias.lias_backend.member.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    private UserStatus status;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    public enum UserStatus {
        ACTIVE, FROZEN, DISABLED, PENDING
    }

    public enum UserRole {
        VISITOR, MEMBER, DOCTORAL, DIRECTOR, ADMIN
    }
}