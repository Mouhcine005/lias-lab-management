package com.lias.lias_backend.history.entity;

import com.lias.lias_backend.member.entity.BaseEntity;
import com.lias.lias_backend.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

// Timeline of a member's status assignments (spec §4/§8/§21 — nothing is
// overwritten without history, statuses like RETIRED/FORMER especially need
// a "since when" record).
@Entity
@Table(name = "member_status_history")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MemberStatusHistory extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Member.MemberStatus status;

    @Column(nullable = false)
    private LocalDate startDate;

    private LocalDate endDate;

    private String changedBy;

    // Optional context, e.g. why someone was moved to RETIRED/FORMER
    @Column(columnDefinition = "TEXT")
    private String reason;

    public boolean isActive() {
        return endDate == null;
    }
}
