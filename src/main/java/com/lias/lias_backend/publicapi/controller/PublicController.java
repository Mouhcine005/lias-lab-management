package com.lias.lias_backend.publicapi.controller;

import com.lias.lias_backend.publicapi.dto.LabInfoResponse;
import com.lias.lias_backend.publicapi.dto.PublicEventResponse;
import com.lias.lias_backend.publicapi.dto.PublicMemberResponse;
import com.lias.lias_backend.publicapi.dto.PublicPublicationResponse;
import com.lias.lias_backend.publicapi.service.PublicService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Unauthenticated, read-only endpoints for visitors (spec §2.1).
// Every response here must go through a Public*Response DTO — never return
// internal entities/DTOs directly, since those carry fields (email, birth
// date, etc.) that must never reach an anonymous caller.
@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicController {

    private final PublicService publicService;

    @GetMapping("/lab-info")
    public ResponseEntity<LabInfoResponse> getLabInfo() {
        return ResponseEntity.ok(publicService.getLabInfo());
    }

    @GetMapping("/teams")
    public ResponseEntity<List<PublicMemberResponse>> getTeams() {
        return ResponseEntity.ok(publicService.getPublicTeams());
    }

    @GetMapping("/events")
    public ResponseEntity<List<PublicEventResponse>> getEvents() {
        return ResponseEntity.ok(publicService.getPublicEvents());
    }

    @GetMapping("/publications")
    public ResponseEntity<List<PublicPublicationResponse>> getPublications() {
        return ResponseEntity.ok(publicService.getPublicPublications());
    }
}
