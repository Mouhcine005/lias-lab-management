package com.lias.lias_backend.governance.entity;

import com.lias.lias_backend.member.entity.BaseEntity;
import com.lias.lias_backend.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "mandates")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Mandate extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MandateRole role;

    @Column(nullable = false)
    private LocalDate startDate;

    private LocalDate endDate;

    private String team;

    public boolean isActive() {
        return endDate == null || endDate.isAfter(LocalDate.now());
    }

    public enum MandateRole {
        DIRECTOR, VICE_DIRECTOR, TEAM_LEADER, TEAM_MEMBER
    }
}