package com.lias.lias_backend.publicapi.dto;

import lombok.Data;

import java.time.LocalDate;

// Public-safe projection of an Event. Excludes organizer email/id — visitors
// don't need internal member references, just the public event details.
@Data
public class PublicEventResponse {
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
}
