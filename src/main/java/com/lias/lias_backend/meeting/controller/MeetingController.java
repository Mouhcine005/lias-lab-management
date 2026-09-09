package com.lias.lias_backend.meeting.controller;

import com.lias.lias_backend.meeting.dto.MeetingRequest;
import com.lias.lias_backend.meeting.dto.MeetingResponse;
import com.lias.lias_backend.meeting.entity.Meeting;
import com.lias.lias_backend.meeting.service.MeetingService;
import jakarta.validation.Valid;
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
@RequestMapping("/api/meetings")
@RequiredArgsConstructor
public class MeetingController {

    private final MeetingService meetingService;

    @GetMapping
    public ResponseEntity<List<MeetingResponse>> getAll() {
        return ResponseEntity.ok(meetingService.getAllMeetings());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MeetingResponse> getOne(@PathVariable Long id) {
        return ResponseEntity.ok(meetingService.getMeeting(id));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<MeetingResponse>> getByStatus(@PathVariable Meeting.MeetingStatus status) {
        return ResponseEntity.ok(meetingService.getByStatus(status));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR')")
    public ResponseEntity<MeetingResponse> create(@Valid @RequestBody MeetingRequest request) {
        return ResponseEntity.ok(meetingService.createMeeting(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR')")
    public ResponseEntity<MeetingResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody MeetingRequest request) {
        return ResponseEntity.ok(meetingService.updateMeeting(id, request));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR')")
    public ResponseEntity<MeetingResponse> updateStatus(
            @PathVariable Long id,
            @RequestParam Meeting.MeetingStatus status) {
        return ResponseEntity.ok(meetingService.updateStatus(id, status));
    }

    @PostMapping(value = "/{id}/pv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR')")
    public ResponseEntity<MeetingResponse> uploadPV(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(meetingService.uploadPV(id, file));
    }

    @GetMapping("/{id}/pv/download")
    public ResponseEntity<Resource> downloadPV(@PathVariable Long id) throws MalformedURLException {
        Path filePath = meetingService.getPVPath(id);
        Resource resource = new UrlResource(filePath.toUri());

        if (!resource.exists())
            throw new RuntimeException("PV file not found");

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        meetingService.deleteMeeting(id);
        return ResponseEntity.noContent().build();
    }
}