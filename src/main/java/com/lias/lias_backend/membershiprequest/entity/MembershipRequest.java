package com.lias.lias_backend.membershiprequest.entity;

import com.lias.lias_backend.member.entity.BaseEntity;
import com.lias.lias_backend.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

// Spec §6 — Gestion des demandes d'adhésion.
// An applicant submits this BEFORE any User/Member account exists. Only on
// ACCEPTED does an account get created (see MembershipRequestService.accept()).
@Entity
@Table(name = "membership_requests")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MembershipRequest extends BaseEntity {

    @Column(nullable = false)
    private String email;

    // Hashed at submission time (BCrypt, same as normal registration) and
    // copied into the new User only if/when this request is accepted.
    @Column(nullable = false)
    private String password;

    private String firstName;
    private String lastName;

    @Column(columnDefinition = "TEXT")
    private String motivationLetter;

    // Stored filename on disk (same convention as Document.filePath), not the
    // original name — avoids collisions, mirrors DocumentService's pattern.
    private String cvPath;
    private String cvOriginalFilename;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Member.MemberStatus requestedStatus;

    private String establishment;
    private String originLaboratory;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus status;

    private LocalDateTime submittedAt;
    private LocalDateTime decidedAt;

    // Email of the director who accepted/rejected
    private String decidedBy;

    @Column(columnDefinition = "TEXT")
    private String rejectionReason;

    // Set once accepted and an account is created from this request
    private Long createdUserId;

    public enum RequestStatus {
        PENDING, ACCEPTED, REJECTED
    }
}
