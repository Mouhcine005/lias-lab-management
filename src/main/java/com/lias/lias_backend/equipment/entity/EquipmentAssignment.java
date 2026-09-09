package com.lias.lias_backend.equipment.entity;

import com.lias.lias_backend.member.entity.BaseEntity;
import com.lias.lias_backend.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "equipment_assignments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EquipmentAssignment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "equipment_id", nullable = false)
    private Equipment equipment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false)
    private Integer quantityAssigned;

    @Column(nullable = false)
    private LocalDate assignmentDate;

    private LocalDate returnDate;

    private String assignmentNote;

    private String returnNote;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_request_id")
    private EquipmentRequest fromRequest;
}