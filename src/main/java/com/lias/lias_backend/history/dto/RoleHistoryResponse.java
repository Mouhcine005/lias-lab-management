package com.lias.lias_backend.history.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class RoleHistoryResponse {
    private Long id;
    private String role;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean active;
    private String changedBy;
}
