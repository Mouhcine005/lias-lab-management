package com.lias.lias_backend.equipment.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EquipmentRequestDto {

    @NotBlank(message = "Equipment name is required")
    private String equipmentName;

    private String equipmentDescription;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantityRequested;

    @NotBlank(message = "Justification is required")
    private String justification;
}