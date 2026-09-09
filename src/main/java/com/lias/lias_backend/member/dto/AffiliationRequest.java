package com.lias.lias_backend.member.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class AffiliationRequest {

    @NotBlank(message = "Laboratory is required")
    private String laboratory;

    private String team;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;
}