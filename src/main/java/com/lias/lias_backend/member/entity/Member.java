package com.lias.lias_backend.member.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "members")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Member extends BaseEntity {

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String firstName;
    private String lastName;
    private String photoPath;
    private String biography;
    private String interests;
    private String establishment;
    private String originLaboratory;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "hire_date")
    private LocalDate hireDate;

    @Enumerated(EnumType.STRING)
    private MemberStatus status;

    public enum MemberStatus {
        PERMANENT, ASSOCIATE, DOCTORAL, RETIRED, FORMER
    }
}