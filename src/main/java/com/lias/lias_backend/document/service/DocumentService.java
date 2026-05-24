package com.lias.lias_backend.document.service;

import com.lias.lias_backend.document.dto.DocumentRequest;
import com.lias.lias_backend.document.dto.DocumentResponse;
import com.lias.lias_backend.document.entity.Document;
import com.lias.lias_backend.document.repository.DocumentRepository;
import com.lias.lias_backend.event.entity.Event;
import com.lias.lias_backend.event.repository.EventRepository;
import com.lias.lias_backend.member.entity.Member;
import com.lias.lias_backend.member.repository.MemberRepository;
import com.lias.lias_backend.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final MemberRepository memberRepository;
    private final EventRepository eventRepository;
    private final NotificationService notificationService;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Transactional
    public DocumentResponse uploadDocument(
            MultipartFile file,
            String description,
            String type,
            Long eventId) throws IOException {

        Member uploader = getCurrentMember();

        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : "";
        String storedFilename = UUID.randomUUID() + extension;
        Path filePath = uploadPath.resolve(storedFilename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        Event event = null;
        if (eventId != null) {
            event = eventRepository.findById(eventId)
                    .orElseThrow(() -> new RuntimeException("Event not found"));
        }

        Document.DocumentType documentType;
        try {
            documentType = type != null
                    ? Document.DocumentType.valueOf(type.toUpperCase())
                    : Document.DocumentType.OTHER;
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid document type: " + type);
        }

        Document document = Document.builder()
                .fileName(originalFilename)
                .filePath(storedFilename)
                .fileType(file.getContentType())
                .fileSize(file.getSize())
                .type(documentType)
                .description(description)
                .uploadedBy(uploader)
                .event(event)
                .build();

        DocumentResponse response = toResponse(documentRepository.save(document));

        // Notify all active members of new document
        notificationService.notifyNewDocument(
                originalFilename,
                event != null ? event.getTitle() : null
        );

        return response;
    }

    @Transactional
    public DocumentResponse updateDocument(Long id, DocumentRequest request) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        document.setDescription(request.getDescription());
        if (request.getType() != null) document.setType(request.getType());

        if (request.getEventId() != null) {
            Event event = eventRepository.findById(request.getEventId())
                    .orElseThrow(() -> new RuntimeException("Event not found"));
            document.setEvent(event);
        }

        return toResponse(documentRepository.save(document));
    }

    public List<DocumentResponse> getAllDocuments() {
        return documentRepository.findAll()
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<DocumentResponse> getByEvent(Long eventId) {
        return documentRepository.findByEventId(eventId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<DocumentResponse> getByType(String type) {
        Document.DocumentType documentType;
        try {
            documentType = Document.DocumentType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid type: " + type);
        }
        return documentRepository.findByType(documentType)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<DocumentResponse> getMyDocuments() {
        Member member = getCurrentMember();
        return documentRepository.findByUploadedById(member.getId())
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public Path getFilePath(Long documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));
        return Paths.get(uploadDir).resolve(document.getFilePath());
    }

    @Transactional
    public void deleteDocument(Long id) throws IOException {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found"));
        Path filePath = Paths.get(uploadDir).resolve(document.getFilePath());
        Files.deleteIfExists(filePath);
        documentRepository.delete(document);
    }

    private Member getCurrentMember() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return memberRepository.findByUserEmail(email)
                .orElseThrow(() -> new RuntimeException("Member not found"));
    }

    private DocumentResponse toResponse(Document d) {
        DocumentResponse r = new DocumentResponse();
        r.setId(d.getId());
        r.setFileName(d.getFileName());
        r.setFileType(d.getFileType());
        r.setFileSize(d.getFileSize());
        r.setType(d.getType() != null ? d.getType().name() : null);
        r.setDescription(d.getDescription());
        r.setDownloadUrl("/api/documents/" + d.getId() + "/download");
        r.setCreatedAt(d.getCreatedAt());

        if (d.getEvent() != null) {
            r.setEventId(d.getEvent().getId());
            r.setEventTitle(d.getEvent().getTitle());
        }

        if (d.getUploadedBy() != null) {
            r.setUploadedById(d.getUploadedBy().getId());
            r.setUploadedByEmail(d.getUploadedBy().getUser().getEmail());
        }

        return r;
    }
}