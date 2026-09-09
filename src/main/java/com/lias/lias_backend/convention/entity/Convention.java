package com.lias.lias_backend.convention.entity;

import com.lias.lias_backend.member.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "conventions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Convention extends BaseEntity {

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String partnerName;

    private String partnerCountry;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private LocalDate startDate;

    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConventionStatus status;

    // path to uploaded convention document (relative to uploadDir)
    private String documentPath;

    public enum ConventionStatus {
        ACTIVE, EXPIRED, PENDING, CANCELLED
    }
}
