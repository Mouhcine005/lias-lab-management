package com.lias.lias_backend.event.entity;

import com.lias.lias_backend.member.entity.BaseEntity;
import com.lias.lias_backend.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "events")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Event extends BaseEntity {

    @Column(nullable = false)
    private String title;

    private String description;
    private String location;

    @Column(nullable = false)
    private LocalDate startDate;

    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventType type;

    @Enumerated(EnumType.STRING)
    private EventStatus status;

    // The member who created/organized this event
    @ManyToOne
    @JoinColumn(name = "organizer_id")
    private Member organizer;

    private String edition;
    private String website;

    public enum EventType {
        CONFERENCE, SEMINAR, WORKSHOP, OTHER
    }

    public enum EventStatus {
        PLANNED, ONGOING, COMPLETED, CANCELLED
    }
}