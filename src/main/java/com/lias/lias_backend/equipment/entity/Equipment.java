package com.lias.lias_backend.equipment.entity;

import com.lias.lias_backend.member.entity.BaseEntity;
import com.lias.lias_backend.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "equipment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Equipment extends BaseEntity {

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(unique = true)
    private String serialNumber;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Integer availableQuantity;

    @Column(nullable = false)
    private LocalDate arrivalDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EquipmentCondition condition;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EquipmentStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to_id")
    private Member assignedTo;

    private LocalDate distributionDate;

    private String notes;

    public enum EquipmentCondition {
        NEW, GOOD, FAIR, POOR, OUT_OF_SERVICE
    }

    public enum EquipmentStatus {
        AVAILABLE, ASSIGNED, PARTIALLY_ASSIGNED, OUT_OF_SERVICE
    }
}