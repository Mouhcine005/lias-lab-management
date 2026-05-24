package com.lias.lias_backend.equipment.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class AssignmentResponse {

    private Long id;
    private Long equipmentId;
    private String equipmentName;
    private String equipmentSerialNumber;
    private Long memberId;
    private String memberName;
    private Integer quantityAssigned;
    private LocalDate assignmentDate;
    private LocalDate returnDate;
    private String assignmentNote;
    private String returnNote;
    private Long fromRequestId;
}