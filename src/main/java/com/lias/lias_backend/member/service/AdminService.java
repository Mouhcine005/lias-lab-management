package com.lias.lias_backend.member.service;

import com.lias.lias_backend.audit.service.AuditService;
import com.lias.lias_backend.history.entity.MemberStatusHistory;
import com.lias.lias_backend.history.entity.RoleHistory;
import com.lias.lias_backend.history.repository.MemberStatusHistoryRepository;
import com.lias.lias_backend.history.repository.RoleHistoryRepository;
import com.lias.lias_backend.member.dto.AdminMemberResponse;
import com.lias.lias_backend.member.entity.Member;
import com.lias.lias_backend.member.entity.User;
import com.lias.lias_backend.member.repository.AffiliationRepository;
import com.lias.lias_backend.member.repository.MemberRepository;
import com.lias.lias_backend.member.repository.UserRepository;
import com.lias.lias_backend.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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
    private final RoleHistoryRepository roleHistoryRepository;
    private final MemberStatusHistoryRepository memberStatusHistoryRepository;

    public List<AdminMemberResponse> getAllMembers() {
        return memberRepository.findAll()
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<AdminMemberResponse> getPendingMembers() {
        return memberRepository.findAll().stream()
                .filter(m -> m.getUser().getStatus() == User.UserStatus.PENDING)
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

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

    @Transactional
    public AdminMemberResponse changeRole(Long userId, String role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        User.UserRole newRole;
        try {
            newRole = User.UserRole.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role: " + role +
                    ". Valid roles: VISITOR, MEMBER, DOCTORAL, DIRECTOR, ADMIN");
        }

        String actingUser = currentUserEmail();
        LocalDate today = LocalDate.now();

        roleHistoryRepository.findByUserIdAndEndDateIsNull(userId)
                .ifPresent(current -> {
                    current.setEndDate(today);
                    roleHistoryRepository.save(current);
                });

        roleHistoryRepository.save(RoleHistory.builder()
                .user(user)
                .role(newRole)
                .startDate(today)
                .changedBy(actingUser)
                .build());

        user.setRole(newRole);
        userRepository.save(user);

        Member member = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Member not found"));
        auditService.log("ROLE_CHANGED", "User", userId,
                "Role changed to " + role.toUpperCase());
        return toResponse(member);
    }

    @Transactional
    public AdminMemberResponse changeMemberStatus(Long userId, String status) {
        Member member = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        Member.MemberStatus newStatus;
        try {
            newStatus = Member.MemberStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status: " + status +
                    ". Valid statuses: PERMANENT, ASSOCIATE, DOCTORAL, RETIRED, FORMER");
        }

        String actingUser = currentUserEmail();
        LocalDate today = LocalDate.now();

        memberStatusHistoryRepository.findByMemberIdAndEndDateIsNull(member.getId())
                .ifPresent(current -> {
                    current.setEndDate(today);
                    memberStatusHistoryRepository.save(current);
                });

        memberStatusHistoryRepository.save(MemberStatusHistory.builder()
                .member(member)
                .status(newStatus)
                .startDate(today)
                .changedBy(actingUser)
                .build());

        member.setStatus(newStatus);
        memberRepository.save(member);
        auditService.log("STATUS_CHANGED", "Member", userId,
                "Member status changed to " + status.toUpperCase());
        return toResponse(member);
    }

    private String currentUserEmail() {
        try {
            return SecurityContextHolder.getContext().getAuthentication().getName();
        } catch (Exception e) {
            return "system";
        }
    }

    public List<com.lias.lias_backend.history.dto.RoleHistoryResponse> getRoleHistory(Long userId) {
        return roleHistoryRepository.findByUserIdOrderByStartDateDesc(userId).stream()
                .map(h -> {
                    var r = new com.lias.lias_backend.history.dto.RoleHistoryResponse();
                    r.setId(h.getId());
                    r.setRole(h.getRole().name());
                    r.setStartDate(h.getStartDate());
                    r.setEndDate(h.getEndDate());
                    r.setActive(h.isActive());
                    r.setChangedBy(h.getChangedBy());
                    return r;
                })
                .collect(Collectors.toList());
    }

    public List<com.lias.lias_backend.history.dto.MemberStatusHistoryResponse> getMemberStatusHistory(Long memberId) {
        return memberStatusHistoryRepository.findByMemberIdOrderByStartDateDesc(memberId).stream()
                .map(h -> {
                    var r = new com.lias.lias_backend.history.dto.MemberStatusHistoryResponse();
                    r.setId(h.getId());
                    r.setStatus(h.getStatus().name());
                    r.setStartDate(h.getStartDate());
                    r.setEndDate(h.getEndDate());
                    r.setActive(h.isActive());
                    r.setChangedBy(h.getChangedBy());
                    r.setReason(h.getReason());
                    return r;
                })
                .collect(Collectors.toList());
    }

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