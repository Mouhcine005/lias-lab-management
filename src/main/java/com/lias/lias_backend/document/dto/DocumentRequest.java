package com.lias.lias_backend.document.dto;

import com.lias.lias_backend.document.entity.Document;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DocumentRequest {

    private String description;

    @NotNull(message = "Document type is required")
    private Document.DocumentType type;

    private Long eventId;
}