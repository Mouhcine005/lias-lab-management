package com.lias.lias_backend.meeting.entity;

import com.lias.lias_backend.member.entity.BaseEntity;
import com.lias.lias_backend.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "meetings")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Meeting extends BaseEntity {

    @Column(nullable = false)
    private String title;

    private String description;
    private LocalDate date;
    private String location;

    @Enumerated(EnumType.STRING)
    private MeetingStatus status;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private Member createdBy;

    // PV (procès-verbal) file path after upload
    private String pvFilePath;
    private String pvFileName;

    // Agenda / order of the day
    @Column(columnDefinition = "TEXT")
    private String agenda;

    public enum MeetingStatus {
        PLANNED, COMPLETED, CANCELLED
    }
}