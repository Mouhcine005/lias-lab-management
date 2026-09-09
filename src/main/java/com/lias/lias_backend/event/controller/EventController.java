package com.lias.lias_backend.event.controller;

import com.lias.lias_backend.event.dto.EventRequest;
import com.lias.lias_backend.event.dto.EventResponse;
import com.lias.lias_backend.event.entity.Event;
import com.lias.lias_backend.event.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    // Anyone authenticated can view events
    @GetMapping
    public ResponseEntity<List<EventResponse>> getAll() {
        return ResponseEntity.ok(eventService.getAllEvents());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> getOne(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.getEvent(id));
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<EventResponse>> getByType(@PathVariable Event.EventType type) {
        return ResponseEntity.ok(eventService.getByType(type));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<EventResponse>> getByStatus(@PathVariable Event.EventStatus status) {
        return ResponseEntity.ok(eventService.getByStatus(status));
    }

    @GetMapping("/me")
    public ResponseEntity<List<EventResponse>> getMyEvents() {
        return ResponseEntity.ok(eventService.getMyEvents());
    }

    // Only ADMIN and DIRECTOR can create/update/delete events
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR')")
    public ResponseEntity<EventResponse> create(@Valid @RequestBody EventRequest request) {
        return ResponseEntity.ok(eventService.createEvent(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR')")
    public ResponseEntity<EventResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody EventRequest request) {
        return ResponseEntity.ok(eventService.updateEvent(id, request));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR')")
    public ResponseEntity<EventResponse> updateStatus(
            @PathVariable Long id,
            @RequestParam Event.EventStatus status) {
        return ResponseEntity.ok(eventService.updateStatus(id, status));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        eventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }
}