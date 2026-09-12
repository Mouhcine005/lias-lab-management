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

    @GetMapping("/members")
    public ResponseEntity<List<AdminMemberResponse>> getAllMembers() {
        return ResponseEntity.ok(adminService.getAllMembers());
    }

    @GetMapping("/members/pending")
    public ResponseEntity<List<AdminMemberResponse>> getPendingMembers() {
        return ResponseEntity.ok(adminService.getPendingMembers());
    }

    @PatchMapping("/members/{userId}/approve")
    public ResponseEntity<AdminMemberResponse> approveMember(@PathVariable Long userId) {
        return ResponseEntity.ok(adminService.approveMember(userId));
    }

    @PatchMapping("/members/{userId}/reject")
    public ResponseEntity<AdminMemberResponse> rejectMember(@PathVariable Long userId) {
        return ResponseEntity.ok(adminService.rejectMember(userId));
    }

    @PatchMapping("/members/{userId}/freeze")
    public ResponseEntity<AdminMemberResponse> freezeMember(@PathVariable Long userId) {
        return ResponseEntity.ok(adminService.freezeMember(userId));
    }

    @PatchMapping("/members/{userId}/activate")
    public ResponseEntity<AdminMemberResponse> activateMember(@PathVariable Long userId) {
        return ResponseEntity.ok(adminService.activateMember(userId));
    }

    @PatchMapping("/members/{userId}/role")
    public ResponseEntity<AdminMemberResponse> changeRole(
            @PathVariable Long userId,
            @RequestParam String role) {
        return ResponseEntity.ok(adminService.changeRole(userId, role));
    }

    @PatchMapping("/members/{memberId}/status")
    public ResponseEntity<AdminMemberResponse> changeMemberStatus(
            @PathVariable Long memberId,
            @RequestParam String status) {
        return ResponseEntity.ok(adminService.changeMemberStatus(memberId, status));
    }

    @GetMapping("/members/{userId}/role-history")
    public ResponseEntity<List<com.lias.lias_backend.history.dto.RoleHistoryResponse>> getRoleHistory(
            @PathVariable Long userId) {
        return ResponseEntity.ok(adminService.getRoleHistory(userId));
    }

    @GetMapping("/members/{memberId}/status-history")
    public ResponseEntity<List<com.lias.lias_backend.history.dto.MemberStatusHistoryResponse>> getMemberStatusHistory(
            @PathVariable Long memberId) {
        return ResponseEntity.ok(adminService.getMemberStatusHistory(memberId));
    }
}