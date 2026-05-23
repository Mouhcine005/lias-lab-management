package com.lias.lias_backend.governance.dto;

import com.lias.lias_backend.governance.entity.Mandate;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class MandateRequest {

    @NotNull(message = "Member ID is required")
    private Long memberId;

    @NotNull(message = "Role is required")
    private Mandate.MandateRole role;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    private LocalDate endDate;
    private String team;
}