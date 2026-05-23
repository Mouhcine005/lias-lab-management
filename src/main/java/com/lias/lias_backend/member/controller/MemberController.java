package com.lias.lias_backend.member.controller;

import com.lias.lias_backend.member.dto.*;
import com.lias.lias_backend.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}