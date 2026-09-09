package com.lias.lias_backend.governance.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class MandateResponse {
    private Long id;
    private Long memberId;
    private String memberFirstName;
    private String memberLastName;
    private String memberEmail;
    private String role;
    private LocalDate startDate;
    private LocalDate endDate;
    private String team;
    private boolean active;
    private LocalDateTime createdAt;
}