package com.lias.lias_backend.member.service;

import com.lias.lias_backend.audit.service.AuditService;
import com.lias.lias_backend.member.dto.AdminMemberResponse;
import com.lias.lias_backend.member.entity.Member;
import com.lias.lias_backend.member.entity.User;
import com.lias.lias_backend.member.repository.AffiliationRepository;
import com.lias.lias_backend.member.repository.MemberRepository;
import com.lias.lias_backend.member.repository.UserRepository;
import com.lias.lias_backend.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final MemberRepository memberRepository;
    private final AffiliationRepository affiliationRepository;
    private final NotificationService notificationService;
    private final AuditService auditService;

    // List all members
    public List<AdminMemberResponse> getAllMembers() {
        return memberRepository.findAll()
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // List only PENDING users (awaiting approval)
    public List<AdminMemberResponse> getPendingMembers() {
        return memberRepository.findAll().stream()
                .filter(m -> m.getUser().getStatus() == User.UserStatus.PENDING)
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // Approve a pending member → set status to ACTIVE
    @Transactional
    public AdminMemberResponse approveMember(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getStatus() != User.UserStatus.PENDING)
            throw new IllegalArgumentException("User is not in PENDING status");

        user.setStatus(User.UserStatus.ACTIVE);
        userRepository.save(user);

        Member member = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        notificationService.notifyMemberApproved(member);
        auditService.log("MEMBER_APPROVED", "Member", member.getId(),
                "Member " + member.getFirstName() + " " + member.getLastName() + " approved");

        return toResponse(member);
    }

    // Reject a pending member → set status to DISABLED
    @Transactional
    public AdminMemberResponse rejectMember(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getStatus() != User.UserStatus.PENDING)
            throw new IllegalArgumentException("User is not in PENDING status");

        user.setStatus(User.UserStatus.DISABLED);
        userRepository.save(user);

        Member member = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        notificationService.notifyMemberRejected(member);
        auditService.log("MEMBER_REJECTED", "Member", member.getId(),
                "Member " + member.getFirstName() + " " + member.getLastName() + " rejected");

        return toResponse(member);
    }

    // Freeze an active member
    @Transactional
    public AdminMemberResponse freezeMember(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getStatus() != User.UserStatus.ACTIVE)
            throw new IllegalArgumentException("User is not ACTIVE");

        user.setStatus(User.UserStatus.FROZEN);
        userRepository.save(user);

        Member member = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Member not found"));
        auditService.log("MEMBER_FROZEN", "User", userId,
                "Member " + member.getFirstName() + " " + member.getLastName() + " frozen");
        return toResponse(member);
    }

    // Reactivate a frozen or disabled member
    @Transactional
    public AdminMemberResponse activateMember(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getStatus() == User.UserStatus.ACTIVE)
            throw new IllegalArgumentException("User is already ACTIVE");

        user.setStatus(User.UserStatus.ACTIVE);
        userRepository.save(user);

        Member member = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Member not found"));
        auditService.log("MEMBER_ACTIVATED", "User", userId,
                "Member " + member.getFirstName() + " " + member.getLastName() + " reactivated");
        return toResponse(member);
    }

    // Change a user's role
    @Transactional
    public AdminMemberResponse changeRole(Long userId, String role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        try {
            user.setRole(User.UserRole.valueOf(role.toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role: " + role +
                    ". Valid roles: VISITOR, MEMBER, DOCTORAL, DIRECTOR, ADMIN");
        }

        userRepository.save(user);

        Member member = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Member not found"));
        auditService.log("ROLE_CHANGED", "User", userId,
                "Role changed to " + role.toUpperCase());
        return toResponse(member);
    }

    // Change a member's status (PERMANENT, ASSOCIATE, etc.)
    @Transactional
    public AdminMemberResponse changeMemberStatus(Long userId, String status) {
        Member member = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        try {
            member.setStatus(Member.MemberStatus.valueOf(status.toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status: " + status +
                    ". Valid statuses: PERMANENT, ASSOCIATE, DOCTORAL, RETIRED, FORMER");
        }

        memberRepository.save(member);
        auditService.log("STATUS_CHANGED", "Member", userId,
                "Member status changed to " + status.toUpperCase());
        return toResponse(member);
    }

    // --- helper ---

    private AdminMemberResponse toResponse(Member member) {
        AdminMemberResponse r = new AdminMemberResponse();
        r.setUserId(member.getUser().getId());
        r.setMemberId(member.getId());
        r.setEmail(member.getUser().getEmail());
        r.setFirstName(member.getFirstName());
        r.setLastName(member.getLastName());
        r.setUserStatus(member.getUser().getStatus().name());
        r.setUserRole(member.getUser().getRole().name());
        r.setMemberStatus(member.getStatus() != null ? member.getStatus().name() : null);
        r.setHireDate(member.getHireDate());

        affiliationRepository.findByMemberIdAndEndDateIsNull(member.getId())
                .ifPresent(a -> {
                    r.setCurrentLaboratory(a.getLaboratory());
                    r.setCurrentTeam(a.getTeam());
                });

        return r;
    }
}