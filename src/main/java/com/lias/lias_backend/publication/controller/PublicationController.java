package com.lias.lias_backend.publication.controller;

import com.lias.lias_backend.publication.dto.PublicationRequest;
import com.lias.lias_backend.publication.dto.PublicationResponse;
import com.lias.lias_backend.publication.service.PublicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/publications")
@RequiredArgsConstructor
public class PublicationController {

    private final PublicationService publicationService;

    // Anyone authenticated can view all publications
    @GetMapping
    public ResponseEntity<List<PublicationResponse>> getAll() {
        return ResponseEntity.ok(publicationService.getAllPublications());
    }

    // Filter by year
    @GetMapping("/year/{year}")
    public ResponseEntity<List<PublicationResponse>> getByYear(@PathVariable Integer year) {
        return ResponseEntity.ok(publicationService.getByYear(year));
    }

    // Filter by team
    @GetMapping("/team/{team}")
    public ResponseEntity<List<PublicationResponse>> getByTeam(@PathVariable String team) {
        return ResponseEntity.ok(publicationService.getByTeam(team));
    }

    // Get my publications
    @GetMapping("/me")
    public ResponseEntity<List<PublicationResponse>> getMyPublications() {
        return ResponseEntity.ok(publicationService.getMyPublications());
    }

    // Get publications by member id
    @GetMapping("/member/{memberId}")
    public ResponseEntity<List<PublicationResponse>> getByMember(@PathVariable Long memberId) {
        return ResponseEntity.ok(publicationService.getByMember(memberId));
    }

    // Add publication (MEMBER, DOCTORAL, DIRECTOR, ADMIN)
    @PostMapping
    @PreAuthorize("hasAnyRole('MEMBER', 'DOCTORAL', 'DIRECTOR', 'ADMIN')")
    public ResponseEntity<PublicationResponse> add(@Valid @RequestBody PublicationRequest request) {
        return ResponseEntity.ok(publicationService.addPublication(request));
    }

    // Update publication (owner only)
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('MEMBER', 'DOCTORAL', 'DIRECTOR', 'ADMIN')")
    public ResponseEntity<PublicationResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody PublicationRequest request) {
        return ResponseEntity.ok(publicationService.updatePublication(id, request));
    }

    // Delete publication (owner only)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('MEMBER', 'DOCTORAL', 'DIRECTOR', 'ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        publicationService.deletePublication(id);
        return ResponseEntity.noContent().build();
    }
}