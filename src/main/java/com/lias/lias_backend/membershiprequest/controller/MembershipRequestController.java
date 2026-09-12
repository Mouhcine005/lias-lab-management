package com.lias.lias_backend.membershiprequest.controller;

import com.lias.lias_backend.membershiprequest.dto.MembershipRequestResponse;
import com.lias.lias_backend.membershiprequest.dto.RejectRequestDto;
import com.lias.lias_backend.membershiprequest.service.MembershipRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/membership-requests")
@RequiredArgsConstructor
public class MembershipRequestController {

    private final MembershipRequestService membershipRequestService;

    // Public submission — no auth (spec §6 "Soumission en ligne").
    // multipart so the CV can be attached alongside the form fields.
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MembershipRequestResponse> submit(
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String motivationLetter,
            @RequestParam String requestedStatus,
            @RequestParam(required = false) String establishment,
            @RequestParam(required = false) String originLaboratory,
            @RequestParam(value = "cv", required = false) MultipartFile cv) throws IOException {
        return ResponseEntity.ok(membershipRequestService.submit(
                email, password, firstName, lastName, motivationLetter,
                requestedStatus, establishment, originLaboratory, cv));
    }

    // List all requests (ADMIN/DIRECTOR)
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR')")
    public ResponseEntity<List<MembershipRequestResponse>> getAll() {
        return ResponseEntity.ok(membershipRequestService.getAll());
    }

    // List pending requests only
    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR')")
    public ResponseEntity<List<MembershipRequestResponse>> getPending() {
        return ResponseEntity.ok(membershipRequestService.getPending());
    }

    // Accept — the service itself enforces "director in active mandate"
    // (spec §6), which is stricter than this role check.
    @PostMapping("/{id}/accept")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR')")
    public ResponseEntity<MembershipRequestResponse> accept(@PathVariable Long id) {
        return ResponseEntity.ok(membershipRequestService.accept(id));
    }

    // Reject
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR')")
    public ResponseEntity<MembershipRequestResponse> reject(
            @PathVariable Long id,
            @RequestBody(required = false) RejectRequestDto body) {
        String reason = body != null ? body.getReason() : null;
        return ResponseEntity.ok(membershipRequestService.reject(id, reason));
    }
}
