package com.lias.lias_backend.event.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class EventResponse {
    private Long id;
    private String title;
    private String description;
    private String location;
    private String edition;
    private String website;
    private LocalDate startDate;
    private LocalDate endDate;
    private String type;
    private String status;
    private Long organizerId;
    private String organizerFirstName;
    private String organizerLastName;
    private String organizerEmail;
    private LocalDateTime createdAt;
}