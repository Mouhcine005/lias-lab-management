package com.lias.lias_backend.convention.service;

import com.lias.lias_backend.audit.service.AuditService;
import com.lias.lias_backend.convention.dto.*;
import com.lias.lias_backend.convention.entity.Convention;
import com.lias.lias_backend.convention.repository.ConventionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
public class ConventionService {

    private final ConventionRepository conventionRepository;
    private final AuditService auditService;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    public List<ConventionResponse> getAll() {
        return conventionRepository.findAll().stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    public ConventionResponse getById(Long id) {
        return toResponse(conventionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Convention not found")));
    }

    public List<ConventionResponse> getByStatus(String status) {
        Convention.ConventionStatus s;
        try {
            s = Convention.ConventionStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status: " + status);
        }
        return conventionRepository.findByStatus(s).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public ConventionResponse create(ConventionRequest req) {
        Convention c = Convention.builder()
                .title(req.getTitle())
                .partnerName(req.getPartnerName())
                .partnerCountry(req.getPartnerCountry())
                .description(req.getDescription())
                .startDate(req.getStartDate())
                .endDate(req.getEndDate())
                .status(Convention.ConventionStatus.valueOf(req.getStatus().toUpperCase()))
                .build();
        Convention saved = conventionRepository.save(c);
        auditService.log("CONVENTION_CREATED", "Convention", saved.getId(),
                "Convention '" + saved.getTitle() + "' with " + saved.getPartnerName() + " created");
        return toResponse(saved);
    }

    @Transactional
    public ConventionResponse update(Long id, ConventionRequest req) {
        Convention c = conventionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Convention not found"));
        if (req.getTitle() != null) c.setTitle(req.getTitle());
        if (req.getPartnerName() != null) c.setPartnerName(req.getPartnerName());
        if (req.getPartnerCountry() != null) c.setPartnerCountry(req.getPartnerCountry());
        if (req.getDescription() != null) c.setDescription(req.getDescription());
        if (req.getStartDate() != null) c.setStartDate(req.getStartDate());
        if (req.getEndDate() != null) c.setEndDate(req.getEndDate());
        if (req.getStatus() != null) {
            c.setStatus(Convention.ConventionStatus.valueOf(req.getStatus().toUpperCase()));
        }
        Convention saved = conventionRepository.save(c);
        auditService.log("CONVENTION_UPDATED", "Convention", saved.getId(),
                "Convention '" + saved.getTitle() + "' updated");
        return toResponse(saved);
    }

    @Transactional
    public ConventionResponse uploadDocument(Long id, MultipartFile file) throws IOException {
        Convention c = conventionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Convention not found"));

        Path dir = Paths.get(uploadDir, "conventions");
        Files.createDirectories(dir);

        String ext = "";
        String orig = file.getOriginalFilename();
        if (orig != null && orig.contains(".")) {
            ext = orig.substring(orig.lastIndexOf("."));
        }
        String storedName = UUID.randomUUID() + ext;
        Path target = dir.resolve(storedName);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        // Store the relative path (under uploadDir) so download can resolve it
        c.setDocumentPath("conventions/" + storedName);
        Convention saved = conventionRepository.save(c);
        auditService.log("CONVENTION_DOC_UPLOADED", "Convention", saved.getId(),
                "Document uploaded: " + orig);
        return toResponse(saved);
    }

    /**
     * Returns the absolute path to the uploaded document file, or null if none.
     * Throws if convention doesn't exist.
     */
    public Path getDocumentPath(Long id) {
        Convention c = conventionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Convention not found"));
        if (c.getDocumentPath() == null) return null;
        return Paths.get(uploadDir).resolve(c.getDocumentPath());
    }

    @Transactional
    public void delete(Long id) {
        if (!conventionRepository.existsById(id)) {
            throw new RuntimeException("Convention not found");
        }
        auditService.log("CONVENTION_DELETED", "Convention", id, "Convention deleted");
        conventionRepository.deleteById(id);
    }

    private ConventionResponse toResponse(Convention c) {
        ConventionResponse r = new ConventionResponse();
        r.setId(c.getId());
        r.setTitle(c.getTitle());
        r.setPartnerName(c.getPartnerName());
        r.setPartnerCountry(c.getPartnerCountry());
        r.setDescription(c.getDescription());
        r.setStartDate(c.getStartDate());
        r.setEndDate(c.getEndDate());
        r.setStatus(c.getStatus().name());
        r.setDocumentPath(c.getDocumentPath());
        r.setDocumentDownloadUrl(c.getDocumentPath() != null
                ? "/api/conventions/" + c.getId() + "/document" : null);
        r.setCreatedAt(c.getCreatedAt());
        return r;
    }
}
