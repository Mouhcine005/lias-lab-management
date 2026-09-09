package com.lias.lias_backend.audit.controller;

import com.lias.lias_backend.audit.entity.AuditLog;
import com.lias.lias_backend.audit.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR')")
public class AuditController {

    private final AuditService auditService;

    @GetMapping
    public ResponseEntity<List<AuditLog>> getAll() {
        return ResponseEntity.ok(auditService.getAll());
    }

    @GetMapping("/entity/{type}/{id}")
    public ResponseEntity<List<AuditLog>> getForEntity(
            @PathVariable String type, @PathVariable Long id) {
        return ResponseEntity.ok(auditService.getForEntity(type, id));
    }

    @GetMapping("/actor/{email}")
    public ResponseEntity<List<AuditLog>> getByActor(@PathVariable String email) {
        return ResponseEntity.ok(auditService.getByActor(email));
    }
}
