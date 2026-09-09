package com.lias.lias_backend.document.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DocumentResponse {
    private Long id;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private String type;
    private String description;
    private Long eventId;
    private String eventTitle;
    private Long uploadedById;
    private String uploadedByEmail;
    private String downloadUrl;
    private LocalDateTime createdAt;
}