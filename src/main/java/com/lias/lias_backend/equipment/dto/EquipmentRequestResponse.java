package com.lias.lias_backend.equipment.dto;

import com.lias.lias_backend.equipment.entity.EquipmentRequest;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class EquipmentRequestResponse {

    private Long id;
    private Long requestedById;
    private String requestedByName;
    private String equipmentName;
    private String equipmentDescription;
    private Integer quantityRequested;
    private String justification;
    private LocalDate requestDate;
    private EquipmentRequest.RequestStatus status;
    private Long validatedById;
    private String validatedByName;
    private LocalDate validationDate;
    private String validationNote;
    private Long fulfilledByEquipmentId;
    private String fulfilledByEquipmentName;
}