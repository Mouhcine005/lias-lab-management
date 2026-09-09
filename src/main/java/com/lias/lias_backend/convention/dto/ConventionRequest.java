package com.lias.lias_backend.convention.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ConventionRequest {
    private String title;
    private String partnerName;
    private String partnerCountry;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
}
