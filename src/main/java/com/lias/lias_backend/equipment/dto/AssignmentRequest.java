package com.lias.lias_backend.equipment.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssignmentRequest {

    @NotNull(message = "Member ID is required")
    private Long memberId;

    @NotNull(message = "Equipment ID is required")
    private Long equipmentId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    private String assignmentNote;

    private Long fromRequestId;
}