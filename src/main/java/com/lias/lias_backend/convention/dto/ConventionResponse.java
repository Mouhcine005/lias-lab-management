package com.lias.lias_backend.convention.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ConventionResponse {
    private Long id;
    private String title;
    private String partnerName;
    private String partnerCountry;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private String documentPath;
    private String documentDownloadUrl;
    private LocalDateTime createdAt;
}
