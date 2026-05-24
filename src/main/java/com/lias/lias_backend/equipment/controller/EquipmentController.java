package com.lias.lias_backend.equipment.controller;

import com.lias.lias_backend.equipment.dto.*;
import com.lias.lias_backend.equipment.entity.EquipmentRequest;
import com.lias.lias_backend.equipment.service.EquipmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/equipment")
@RequiredArgsConstructor
public class EquipmentController {

    private final EquipmentService equipmentService;

    // ── INVENTORY (ADMIN) ─────────────────────────────────────

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EquipmentResponse> addEquipment(@Valid @RequestBody EquipmentArrivalRequest dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(equipmentService.addEquipment(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EquipmentResponse> updateEquipment(
            @PathVariable Long id, @Valid @RequestBody EquipmentArrivalRequest dto) {
        return ResponseEntity.ok(equipmentService.updateEquipment(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteEquipment(@PathVariable Long id) {
        equipmentService.deleteEquipment(id);
        return ResponseEntity.noContent().build();
    }

    // ── QUERIES (any authenticated) ───────────────────────────

    @GetMapping
    public ResponseEntity<List<EquipmentResponse>> getAllEquipment(
            @RequestParam(required = false) String search) {
        if (search != null && !search.isBlank()) {
            return ResponseEntity.ok(equipmentService.searchEquipment(search));
        }
        return ResponseEntity.ok(equipmentService.getAllEquipment());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EquipmentResponse> getEquipmentById(@PathVariable Long id) {
        return ResponseEntity.ok(equipmentService.getEquipmentById(id));
    }

    @GetMapping("/available")
    public ResponseEntity<List<EquipmentResponse>> getAvailableEquipment() {
        return ResponseEntity.ok(equipmentService.getAvailableEquipment());
    }

    // ── DISTRIBUTION (ADMIN) ──────────────────────────────────

    @PostMapping("/assignments")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AssignmentResponse> assignEquipment(@Valid @RequestBody AssignmentRequest dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(equipmentService.assignEquipment(dto));
    }

    @PatchMapping("/assignments/{id}/return")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AssignmentResponse> returnEquipment(
            @PathVariable Long id, @RequestBody ReturnRequest dto) {
        return ResponseEntity.ok(equipmentService.returnEquipment(id, dto));
    }

    @GetMapping("/assignments")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR')")
    public ResponseEntity<List<AssignmentResponse>> getAllActiveAssignments() {
        return ResponseEntity.ok(equipmentService.getAllActiveAssignments());
    }

    @GetMapping("/assignments/member/{memberId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR')")
    public ResponseEntity<List<AssignmentResponse>> getAssignmentsByMember(@PathVariable Long memberId) {
        return ResponseEntity.ok(equipmentService.getAssignmentsByMember(memberId));
    }

    @GetMapping("/{id}/assignments")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR')")
    public ResponseEntity<List<AssignmentResponse>> getAssignmentsByEquipment(@PathVariable Long id) {
        return ResponseEntity.ok(equipmentService.getAssignmentsByEquipment(id));
    }

    @GetMapping("/assignments/no-equipment")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR')")
    public ResponseEntity<List<Long>> getMembersWithNoEquipment() {
        return ResponseEntity.ok(equipmentService.getMemberIdsWithNoEquipment());
    }

    // ── REQUESTS (any authenticated) ──────────────────────────

    @PostMapping("/requests")
    public ResponseEntity<EquipmentRequestResponse> submitRequest(@Valid @RequestBody EquipmentRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(equipmentService.submitRequest(dto));
    }

    @GetMapping("/requests/my")
    public ResponseEntity<List<EquipmentRequestResponse>> getMyRequests() {
        return ResponseEntity.ok(equipmentService.getMyRequests());
    }

    @GetMapping("/requests")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR')")
    public ResponseEntity<List<EquipmentRequestResponse>> getAllRequests(
            @RequestParam(required = false) EquipmentRequest.RequestStatus status) {
        return ResponseEntity.ok(equipmentService.getAllRequests(status));
    }

    // ── VALIDATION (DIRECTOR or ADMIN) ────────────────────────

    @PatchMapping("/requests/{id}/validate")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR')")
    public ResponseEntity<EquipmentRequestResponse> validateRequest(
            @PathVariable Long id, @Valid @RequestBody ValidationRequest dto) {
        return ResponseEntity.ok(equipmentService.validateRequest(id, dto));
    }
}