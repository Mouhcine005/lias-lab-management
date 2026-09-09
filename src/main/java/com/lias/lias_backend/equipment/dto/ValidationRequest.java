package com.lias.lias_backend.equipment.dto;

import com.lias.lias_backend.equipment.entity.EquipmentRequest;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ValidationRequest {

    @NotNull(message = "Decision is required (APPROVED or REJECTED)")
    private EquipmentRequest.RequestStatus decision;

    private String validationNote;

    private Long equipmentId;
}