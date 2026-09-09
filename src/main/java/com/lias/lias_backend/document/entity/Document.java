package com.lias.lias_backend.document.entity;

import com.lias.lias_backend.event.entity.Event;
import com.lias.lias_backend.member.entity.BaseEntity;
import com.lias.lias_backend.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "documents")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Document extends BaseEntity {

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String filePath;

    private String fileType;
    private Long fileSize;

    @Enumerated(EnumType.STRING)
    private DocumentType type;

    @ManyToOne
    @JoinColumn(name = "uploaded_by")
    private Member uploadedBy;

    @ManyToOne
    @JoinColumn(name = "event_id")
    private Event event;

    private String description;

    public enum DocumentType {
        FUNDING_REQUEST, PROGRAM, CERTIFICATE, REPORT, ADMINISTRATIVE, OTHER
    }
}