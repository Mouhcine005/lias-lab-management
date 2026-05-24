package com.lias.lias_backend.meeting.service;

import com.lias.lias_backend.meeting.dto.MeetingRequest;
import com.lias.lias_backend.meeting.dto.MeetingResponse;
import com.lias.lias_backend.meeting.entity.Meeting;
import com.lias.lias_backend.meeting.repository.MeetingRepository;
import com.lias.lias_backend.member.entity.Member;
import com.lias.lias_backend.member.repository.MemberRepository;
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
public class MeetingService {

    private final MeetingRepository meetingRepository;
    private final MemberRepository memberRepository;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    // Get all meetings
    public List<MeetingResponse> getAllMeetings() {
        return meetingRepository.findByOrderByDateDesc()
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // Get meeting by id
    public MeetingResponse getMeeting(Long id) {
        Meeting meeting = meetingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Meeting not found"));
        return toResponse(meeting);
    }

    // Get by status
    public List<MeetingResponse> getByStatus(Meeting.MeetingStatus status) {
        return meetingRepository.findByStatus(status)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // Create meeting
    @Transactional
    public MeetingResponse createMeeting(MeetingRequest request) {
        Member creator = getCurrentMember();

        Meeting meeting = Meeting.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .location(request.getLocation())
                .agenda(request.getAgenda())
                .date(request.getDate())
                .status(request.getStatus() != null ? request.getStatus() : Meeting.MeetingStatus.PLANNED)
                .createdBy(creator)
                .build();

        return toResponse(meetingRepository.save(meeting));
    }

    // Update meeting
    @Transactional
    public MeetingResponse updateMeeting(Long id, MeetingRequest request) {
        Meeting meeting = meetingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Meeting not found"));

        meeting.setTitle(request.getTitle());
        meeting.setDescription(request.getDescription());
        meeting.setLocation(request.getLocation());
        meeting.setAgenda(request.getAgenda());
        meeting.setDate(request.getDate());
        if (request.getStatus() != null) meeting.setStatus(request.getStatus());

        return toResponse(meetingRepository.save(meeting));
    }

    // Upload PV
    @Transactional
    public MeetingResponse uploadPV(Long meetingId, MultipartFile file) throws IOException {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new RuntimeException("Meeting not found"));

        Path uploadPath = Paths.get(uploadDir, "pv");
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

        meeting.setPvFilePath("pv/" + storedFilename);
        meeting.setPvFileName(originalFilename);
        meeting.setStatus(Meeting.MeetingStatus.COMPLETED);

        return toResponse(meetingRepository.save(meeting));
    }

    // Download PV
    public Path getPVPath(Long meetingId) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new RuntimeException("Meeting not found"));
        if (meeting.getPvFilePath() == null)
            throw new RuntimeException("No PV uploaded for this meeting");
        return Paths.get(uploadDir).resolve(meeting.getPvFilePath());
    }

    // Update status
    @Transactional
    public MeetingResponse updateStatus(Long id, Meeting.MeetingStatus status) {
        Meeting meeting = meetingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Meeting not found"));
        meeting.setStatus(status);
        return toResponse(meetingRepository.save(meeting));
    }

    // Delete meeting
    @Transactional
    public void deleteMeeting(Long id) {
        if (!meetingRepository.existsById(id))
            throw new RuntimeException("Meeting not found");
        meetingRepository.deleteById(id);
    }

    // --- helpers ---

    private Member getCurrentMember() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return memberRepository.findByUserEmail(email)
                .orElseThrow(() -> new RuntimeException("Member not found"));
    }

    private MeetingResponse toResponse(Meeting m) {
        MeetingResponse r = new MeetingResponse();
        r.setId(m.getId());
        r.setTitle(m.getTitle());
        r.setDescription(m.getDescription());
        r.setLocation(m.getLocation());
        r.setAgenda(m.getAgenda());
        r.setDate(m.getDate());
        r.setStatus(m.getStatus() != null ? m.getStatus().name() : null);
        r.setPvFileName(m.getPvFileName());
        r.setPvDownloadUrl(m.getPvFilePath() != null ? "/api/meetings/" + m.getId() + "/pv/download" : null);
        r.setCreatedAt(m.getCreatedAt());

        if (m.getCreatedBy() != null) {
            r.setCreatedById(m.getCreatedBy().getId());
            r.setCreatedByEmail(m.getCreatedBy().getUser().getEmail());
            r.setCreatedByFirstName(m.getCreatedBy().getFirstName());
            r.setCreatedByLastName(m.getCreatedBy().getLastName());
        }

        return r;
    }
}