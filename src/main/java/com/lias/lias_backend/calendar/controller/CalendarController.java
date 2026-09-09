package com.lias.lias_backend.calendar.controller;

import com.lias.lias_backend.calendar.dto.CalendarEvent;
import com.lias.lias_backend.event.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/calendar")
@RequiredArgsConstructor
public class CalendarController {

    private final EventRepository eventRepository;

    // Returns all events in a calendar-ready format
    // Optional query params: ?from=2025-01-01&to=2025-12-31
    @GetMapping
    public ResponseEntity<List<CalendarEvent>> getCalendar(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        List<CalendarEvent> events = eventRepository.findAll().stream()
                .filter(e -> {
                    if (from != null && e.getStartDate() != null && e.getStartDate().isBefore(from)) return false;
                    if (to != null && e.getStartDate() != null && e.getStartDate().isAfter(to)) return false;
                    return true;
                })
                .map(e -> new CalendarEvent(
                        e.getId(),
                        e.getTitle(),
                        e.getStartDate(),
                        e.getEndDate(),
                        e.getType() != null ? e.getType().name() : null,
                        e.getLocation()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(events);
    }
}
