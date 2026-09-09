package com.lias.lias_backend.member.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "affiliations")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Affiliation extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    private String laboratory;
    private String team;

    @Column(nullable = false)
    private LocalDate startDate;

    private LocalDate endDate;

    public boolean isActive() {
        return endDate == null;
    }
}