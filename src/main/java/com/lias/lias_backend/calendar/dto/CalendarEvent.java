package com.lias.lias_backend.calendar.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class CalendarEvent {
    private Long id;
    private String title;
    private LocalDate start;
    private LocalDate end;
    private String type;       // CONFERENCE, SEMINAR, WORKSHOP, OTHER
    private String location;
}
