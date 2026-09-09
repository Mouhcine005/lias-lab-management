package com.lias.lias_backend.messaging.entity;

import com.lias.lias_backend.member.entity.BaseEntity;
import com.lias.lias_backend.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "message_threads")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MessageThread extends BaseEntity {

    @Column(nullable = false)
    private String title;

    // GENERAL (lab-wide), TEAM (team-scoped), EVENT (linked to an event)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ThreadType type;

    // optional: filled when type = TEAM
    private String team;

    // optional: filled when type = EVENT
    private Long eventId;

    @ManyToMany
    @JoinTable(
        name = "thread_participants",
        joinColumns = @JoinColumn(name = "thread_id"),
        inverseJoinColumns = @JoinColumn(name = "member_id")
    )
    @Builder.Default
    private List<Member> participants = new ArrayList<>();

    @OneToMany(mappedBy = "thread", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Message> messages = new ArrayList<>();

    public enum ThreadType {
        GENERAL, TEAM, EVENT
    }
}
