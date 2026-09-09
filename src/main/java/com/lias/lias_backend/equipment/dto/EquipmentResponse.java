package com.lias.lias_backend.equipment.dto;

import com.lias.lias_backend.equipment.entity.Equipment;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class EquipmentResponse {

    private Long id;
    private String name;
    private String description;
    private String serialNumber;
    private Integer quantity;
    private Integer availableQuantity;
    private LocalDate arrivalDate;
    private Equipment.EquipmentCondition condition;
    private Equipment.EquipmentStatus status;
    private String notes;
    private Long assignedToId;
    private String assignedToName;
    private LocalDate distributionDate;
}