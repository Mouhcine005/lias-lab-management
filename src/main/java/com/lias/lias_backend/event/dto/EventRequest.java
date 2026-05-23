package com.lias.lias_backend.event.dto;

import com.lias.lias_backend.event.entity.Event;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class EventRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;
    private String location;
    private String edition;
    private String website;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    private LocalDate endDate;

    @NotNull(message = "Event type is required")
    private Event.EventType type;

    private Event.EventStatus status;
}