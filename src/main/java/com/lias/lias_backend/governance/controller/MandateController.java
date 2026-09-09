package com.lias.lias_backend.governance.controller;

import com.lias.lias_backend.governance.dto.MandateRequest;
import com.lias.lias_backend.governance.dto.MandateResponse;
import com.lias.lias_backend.governance.entity.Mandate;
import com.lias.lias_backend.governance.service.MandateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mandates")
@RequiredArgsConstructor
public class MandateController {

    private final MandateService mandateService;

    // Anyone authenticated can view mandates
    @GetMapping
    public ResponseEntity<List<MandateResponse>> getAll() {
        return ResponseEntity.ok(mandateService.getAllMandates());
    }

    @GetMapping("/active")
    public ResponseEntity<List<MandateResponse>> getActive() {
        return ResponseEntity.ok(mandateService.getActiveMandates());
    }

    @GetMapping("/director")
    public ResponseEntity<MandateResponse> getCurrentDirector() {
        return ResponseEntity.ok(mandateService.getCurrentDirector());
    }

    @GetMapping("/vice-director")
    public ResponseEntity<MandateResponse> getCurrentViceDirector() {
        return ResponseEntity.ok(mandateService.getCurrentViceDirector());
    }

    @GetMapping("/member/{memberId}")
    public ResponseEntity<List<MandateResponse>> getByMember(@PathVariable Long memberId) {
        return ResponseEntity.ok(mandateService.getByMember(memberId));
    }

    @GetMapping("/role/{role}")
    public ResponseEntity<List<MandateResponse>> getByRole(@PathVariable Mandate.MandateRole role) {
        return ResponseEntity.ok(mandateService.getByRole(role));
    }

    // Only ADMIN can create/end/delete mandates
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MandateResponse> create(@Valid @RequestBody MandateRequest request) {
        return ResponseEntity.ok(mandateService.createMandate(request));
    }

    @PatchMapping("/{id}/end")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MandateResponse> end(@PathVariable Long id) {
        return ResponseEntity.ok(mandateService.endMandate(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        mandateService.deleteMandate(id);
        return ResponseEntity.noContent().build();
    }
}