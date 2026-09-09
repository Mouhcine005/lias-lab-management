package com.lias.lias_backend.member.controller;

import com.lias.lias_backend.member.dto.*;
import com.lias.lias_backend.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/me")
    public ResponseEntity<MemberProfileResponse> getMyProfile() {
        return ResponseEntity.ok(memberService.getMyProfile());
    }

    @PutMapping("/me")
    public ResponseEntity<MemberProfileResponse> updateMyProfile(
            @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(memberService.updateMyProfile(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MemberProfileResponse> getMember(@PathVariable Long id) {
        return ResponseEntity.ok(memberService.getProfile(id));
    }

    /**
     * POST /api/members/me/photo
     * Upload or replace my profile photo.
     */
    @PostMapping(value = "/me/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MemberProfileResponse> uploadPhoto(
            @RequestPart("photo") MultipartFile photo) throws IOException {
        return ResponseEntity.ok(memberService.uploadPhoto(photo));
    }

    /**
     * GET /api/members/{id}/photo
     * Serve the profile photo of any member.
     */
    @GetMapping("/{id}/photo")
    public ResponseEntity<byte[]> getPhoto(@PathVariable Long id) throws IOException {
        return memberService.getPhoto(id);
    }
}