package com.lias.lias_backend.equipment.dto;

import com.lias.lias_backend.equipment.entity.Equipment;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class EquipmentArrivalRequest {

    @NotBlank(message = "Equipment name is required")
    private String name;

    private String description;

    @NotBlank(message = "Serial number is required")
    private String serialNumber;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    @NotNull(message = "Arrival date is required")
    private LocalDate arrivalDate;

    @NotNull(message = "Condition is required")
    private Equipment.EquipmentCondition condition;

    private String notes;
}