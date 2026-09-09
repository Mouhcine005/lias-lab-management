package com.lias.lias_backend.equipment.entity;

import com.lias.lias_backend.member.entity.BaseEntity;
import com.lias.lias_backend.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "equipment_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EquipmentRequest extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requested_by_id", nullable = false)
    private Member requestedBy;

    @Column(nullable = false)
    private String equipmentName;

    private String equipmentDescription;

    @Column(nullable = false)
    private Integer quantityRequested;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String justification;

    @Column(nullable = false)
    private LocalDate requestDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "validated_by_id")
    private Member validatedBy;

    private LocalDate validationDate;

    private String validationNote;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fulfilled_by_equipment_id")
    private Equipment fulfilledByEquipment;

    public enum RequestStatus {
        PENDING, APPROVED, REJECTED, FULFILLED
    }
}