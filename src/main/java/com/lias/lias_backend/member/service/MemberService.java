package com.lias.lias_backend.member.service;

import com.lias.lias_backend.member.dto.*;
import com.lias.lias_backend.member.entity.*;
import com.lias.lias_backend.member.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final AffiliationRepository affiliationRepository;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    // ── PROFILE ───────────────────────────────────────────────

    public MemberProfileResponse getProfile(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));
        return toResponse(member);
    }

    public MemberProfileResponse getMyProfile() {
        return toResponse(getCurrentMember());
    }

    @Transactional
    public MemberProfileResponse updateMyProfile(UpdateProfileRequest request) {
        Member member = getCurrentMember();

        if (request.getFirstName() != null) member.setFirstName(request.getFirstName());
        if (request.getLastName() != null) member.setLastName(request.getLastName());
        if (request.getBiography() != null) member.setBiography(request.getBiography());
        if (request.getInterests() != null) member.setInterests(request.getInterests());
        if (request.getEstablishment() != null) member.setEstablishment(request.getEstablishment());
        if (request.getOriginLaboratory() != null) member.setOriginLaboratory(request.getOriginLaboratory());

        return toResponse(memberRepository.save(member));
    }

    // ── PHOTO UPLOAD ──────────────────────────────────────────

    @Transactional
    public MemberProfileResponse uploadPhoto(MultipartFile file) throws IOException {
        // Validate it's an image
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("File must be an image (jpg, png, etc.)");
        }

        // Validate size — max 5MB
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("Photo must be under 5MB");
        }

        Member member = getCurrentMember();

        // Delete old photo if exists
        if (member.getPhotoPath() != null) {
            Path oldPath = Paths.get(uploadDir).resolve(member.getPhotoPath());
            Files.deleteIfExists(oldPath);
        }

        // Save new photo to uploads/photos/
        Path photoDir = Paths.get(uploadDir, "photos");
        if (!Files.exists(photoDir)) {
            Files.createDirectories(photoDir);
        }

        String originalFilename = file.getOriginalFilename();
        String extension = (originalFilename != null && originalFilename.contains("."))
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : ".jpg";
        String storedFilename = "photos/" + UUID.randomUUID() + extension;

        Files.copy(file.getInputStream(),
                Paths.get(uploadDir).resolve(storedFilename),
                StandardCopyOption.REPLACE_EXISTING);

        member.setPhotoPath(storedFilename);
        return toResponse(memberRepository.save(member));
    }

    public ResponseEntity<byte[]> getPhoto(Long memberId) throws IOException {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        if (member.getPhotoPath() == null) {
            return ResponseEntity.notFound().build();
        }

        Path photoPath = Paths.get(uploadDir).resolve(member.getPhotoPath());
        if (!Files.exists(photoPath)) {
            return ResponseEntity.notFound().build();
        }

        byte[] imageBytes = Files.readAllBytes(photoPath);

        // Detect content type from extension
        String filename = photoPath.getFileName().toString().toLowerCase();
        MediaType mediaType = MediaType.IMAGE_JPEG;
        if (filename.endsWith(".png")) mediaType = MediaType.IMAGE_PNG;
        else if (filename.endsWith(".gif")) mediaType = MediaType.IMAGE_GIF;
        else if (filename.endsWith(".webp")) mediaType = new MediaType("image", "webp");

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + photoPath.getFileName() + "\"")
                .contentType(mediaType)
                .body(imageBytes);
    }

    // ── HELPERS ───────────────────────────────────────────────

    private Member getCurrentMember() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return memberRepository.findByUserEmail(email)
                .orElseThrow(() -> new RuntimeException("Member not found"));
    }

    private MemberProfileResponse toResponse(Member member) {
        MemberProfileResponse response = new MemberProfileResponse();
        response.setId(member.getId());
        response.setEmail(member.getUser().getEmail());
        response.setFirstName(member.getFirstName());
        response.setLastName(member.getLastName());
        response.setPhotoPath(member.getPhotoPath() != null
                ? "/api/members/" + member.getId() + "/photo"
                : null);
        response.setBiography(member.getBiography());
        response.setInterests(member.getInterests());
        response.setEstablishment(member.getEstablishment());
        response.setOriginLaboratory(member.getOriginLaboratory());
        response.setHireDate(member.getHireDate());
        response.setStatus(member.getStatus());
        response.setRole(member.getUser().getRole().name());

        affiliationRepository.findByMemberIdAndEndDateIsNull(member.getId())
                .ifPresent(a -> {
                    response.setCurrentTeam(a.getTeam());
                    response.setCurrentLaboratory(a.getLaboratory());
                });

        return response;
    }
}