package com.lias.lias_backend.member.controller;

import com.lias.lias_backend.member.dto.AdminMemberResponse;
import com.lias.lias_backend.member.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR')")
public class AdminController {

    private final AdminService adminService;

    // List all members
    @GetMapping("/members")
    public ResponseEntity<List<AdminMemberResponse>> getAllMembers() {
        return ResponseEntity.ok(adminService.getAllMembers());
    }

    // List pending approvals
    @GetMapping("/members/pending")
    public ResponseEntity<List<AdminMemberResponse>> getPendingMembers() {
        return ResponseEntity.ok(adminService.getPendingMembers());
    }

    // Approve a pending member
    @PatchMapping("/members/{userId}/approve")
    public ResponseEntity<AdminMemberResponse> approveMember(@PathVariable Long userId) {
        return ResponseEntity.ok(adminService.approveMember(userId));
    }

    // Reject a pending member
    @PatchMapping("/members/{userId}/reject")
    public ResponseEntity<AdminMemberResponse> rejectMember(@PathVariable Long userId) {
        return ResponseEntity.ok(adminService.rejectMember(userId));
    }

    // Freeze an active member
    @PatchMapping("/members/{userId}/freeze")
    public ResponseEntity<AdminMemberResponse> freezeMember(@PathVariable Long userId) {
        return ResponseEntity.ok(adminService.freezeMember(userId));
    }

    // Reactivate a frozen/disabled member
    @PatchMapping("/members/{userId}/activate")
    public ResponseEntity<AdminMemberResponse> activateMember(@PathVariable Long userId) {
        return ResponseEntity.ok(adminService.activateMember(userId));
    }

    // Change user role (MEMBER, DIRECTOR, ADMIN etc.)
    @PatchMapping("/members/{userId}/role")
    public ResponseEntity<AdminMemberResponse> changeRole(
            @PathVariable Long userId,
            @RequestParam String role) {
        return ResponseEntity.ok(adminService.changeRole(userId, role));
    }

    // Change member status (PERMANENT, ASSOCIATE, DOCTORAL etc.)
    @PatchMapping("/members/{memberId}/status")
    public ResponseEntity<AdminMemberResponse> changeMemberStatus(
            @PathVariable Long memberId,
            @RequestParam String status) {
        return ResponseEntity.ok(adminService.changeMemberStatus(memberId, status));
    }
}