package com.lias.lias_backend.publication.entity;

import com.lias.lias_backend.member.entity.BaseEntity;
import com.lias.lias_backend.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "publications")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Publication extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false)
    private String title;

    private String journal;
    private String conference;
    private String doi;

    @Enumerated(EnumType.STRING)
    private PublicationType type;

    @Column(nullable = false)
    private Integer year;

    private String team;
    private String authors;
    private String abstractText;
    private String url;

    public enum PublicationType {
        JOURNAL, CONFERENCE, BOOK, THESIS, OTHER
    }
}