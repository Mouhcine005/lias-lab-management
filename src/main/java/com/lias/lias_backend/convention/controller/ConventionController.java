package com.lias.lias_backend.convention.controller;

import com.lias.lias_backend.convention.dto.*;
import com.lias.lias_backend.convention.service.ConventionService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@RestController
@RequestMapping("/api/conventions")
@RequiredArgsConstructor
public class ConventionController {

    private final ConventionService conventionService;

    // Public reads — visitors can see conventions
    @GetMapping
    public ResponseEntity<List<ConventionResponse>> getAll() {
        return ResponseEntity.ok(conventionService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConventionResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(conventionService.getById(id));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<ConventionResponse>> getByStatus(@PathVariable String status) {
        return ResponseEntity.ok(conventionService.getByStatus(status));
    }

    // Download the uploaded convention document
    @GetMapping("/{id}/document")
    public ResponseEntity<Resource> downloadDocument(@PathVariable Long id) throws MalformedURLException {
        Path filePath = conventionService.getDocumentPath(id);
        if (filePath == null || !Files.exists(filePath)) {
            return ResponseEntity.notFound().build();
        }
        Resource resource = new UrlResource(filePath.toUri());
        String contentType;
        try {
            contentType = Files.probeContentType(filePath);
        } catch (IOException e) {
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
        if (contentType == null) contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + filePath.getFileName() + "\"")
                .body(resource);
    }

    // Director/Admin only for write operations
    @PostMapping
    @PreAuthorize("hasAnyRole('DIRECTOR', 'ADMIN')")
    public ResponseEntity<ConventionResponse> create(@RequestBody ConventionRequest req) {
        return ResponseEntity.ok(conventionService.create(req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('DIRECTOR', 'ADMIN')")
    public ResponseEntity<ConventionResponse> update(
            @PathVariable Long id, @RequestBody ConventionRequest req) {
        return ResponseEntity.ok(conventionService.update(id, req));
    }

    @PostMapping(value = "/{id}/document", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('DIRECTOR', 'ADMIN')")
    public ResponseEntity<ConventionResponse> uploadDocument(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(conventionService.uploadDocument(id, file));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('DIRECTOR', 'ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        conventionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
