package com.lias.lias_backend.history.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class MemberStatusHistoryResponse {
    private Long id;
    private String status;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean active;
    private String changedBy;
    private String reason;
}
