package com.lias.lias_backend.document.controller;

import com.lias.lias_backend.document.dto.DocumentRequest;
import jakarta.validation.Valid;
import com.lias.lias_backend.document.dto.DocumentResponse;
import com.lias.lias_backend.document.service.DocumentService;
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
import java.nio.file.Path;
import java.util.List;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    // Upload a document
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentResponse> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "eventId", required = false) Long eventId) throws IOException {
        return ResponseEntity.ok(documentService.uploadDocument(file, description, type, eventId));
    }

    // Get all documents
    @GetMapping
    public ResponseEntity<List<DocumentResponse>> getAll() {
        return ResponseEntity.ok(documentService.getAllDocuments());
    }

    // Get documents by event
    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<DocumentResponse>> getByEvent(@PathVariable Long eventId) {
        return ResponseEntity.ok(documentService.getByEvent(eventId));
    }

    // Get documents by type
    @GetMapping("/type/{type}")
    public ResponseEntity<List<DocumentResponse>> getByType(@PathVariable String type) {
        return ResponseEntity.ok(documentService.getByType(type));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR')")
    public ResponseEntity<DocumentResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody DocumentRequest request) throws IOException {
        return ResponseEntity.ok(documentService.updateDocument(id, request));
    }

    // Get my documents
    @GetMapping("/me")
    public ResponseEntity<List<DocumentResponse>> getMyDocuments() {
        return ResponseEntity.ok(documentService.getMyDocuments());
    }

    // Download a document
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable Long id) throws MalformedURLException {
        Path filePath = documentService.getFilePath(id);
        Resource resource = new UrlResource(filePath.toUri());

        if (!resource.exists())
            throw new RuntimeException("File not found");

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    // Delete a document (ADMIN, DIRECTOR only)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR')")
    public ResponseEntity<Void> delete(@PathVariable Long id) throws IOException {
        documentService.deleteDocument(id);
        return ResponseEntity.noContent().build();
    }
}