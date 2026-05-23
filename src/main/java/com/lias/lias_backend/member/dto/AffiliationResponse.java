package com.lias.lias_backend.member.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class AffiliationResponse {
    private Long id;
    private String laboratory;
    private String team;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean active;
}