package com.lias.lias_backend.member.controller;

import com.lias.lias_backend.member.dto.AffiliationRequest;
import com.lias.lias_backend.member.dto.AffiliationResponse;
import com.lias.lias_backend.member.service.AffiliationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/affiliations")
@RequiredArgsConstructor
public class AffiliationController {

    private final AffiliationService affiliationService;

    // Any authenticated member sees their own history
    @GetMapping("/me")
    public ResponseEntity<List<AffiliationResponse>> getMyAffiliations() {
        return ResponseEntity.ok(affiliationService.getMyAffiliations());
    }

    // Admin/Director sees any member's history
    @GetMapping("/member/{memberId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR')")
    public ResponseEntity<List<AffiliationResponse>> getMemberAffiliations(@PathVariable Long memberId) {
        return ResponseEntity.ok(affiliationService.getAffiliations(memberId));
    }

    // Admin/Director adds a new affiliation for a member
    @PostMapping("/member/{memberId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR')")
    public ResponseEntity<AffiliationResponse> addAffiliation(
            @PathVariable Long memberId,
            @Valid @RequestBody AffiliationRequest request) {
        return ResponseEntity.ok(affiliationService.addAffiliation(memberId, request));
    }

    // Admin/Director closes a member's active affiliation
    @PatchMapping("/member/{memberId}/close")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR')")
    public ResponseEntity<AffiliationResponse> closeAffiliation(@PathVariable Long memberId) {
        return ResponseEntity.ok(affiliationService.closeAffiliation(memberId));
    }
}