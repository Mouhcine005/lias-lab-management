package com.lias.lias_backend.membershiprequest.service;

import com.lias.lias_backend.governance.repository.MandateRepository;
import com.lias.lias_backend.governance.entity.Mandate;
import com.lias.lias_backend.history.entity.MemberStatusHistory;
import com.lias.lias_backend.history.entity.RoleHistory;
import com.lias.lias_backend.history.repository.MemberStatusHistoryRepository;
import com.lias.lias_backend.history.repository.RoleHistoryRepository;
import com.lias.lias_backend.member.entity.Affiliation;
import com.lias.lias_backend.member.entity.Member;
import com.lias.lias_backend.member.entity.User;
import com.lias.lias_backend.member.repository.AffiliationRepository;
import com.lias.lias_backend.member.repository.MemberRepository;
import com.lias.lias_backend.member.repository.UserRepository;
import com.lias.lias_backend.membershiprequest.dto.MembershipRequestResponse;
import com.lias.lias_backend.membershiprequest.entity.MembershipRequest;
import com.lias.lias_backend.membershiprequest.repository.MembershipRequestRepository;
import com.lias.lias_backend.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MembershipRequestService {

    private final MembershipRequestRepository membershipRequestRepository;
    private final UserRepository userRepository;
    private final MemberRepository memberRepository;
    private final AffiliationRepository affiliationRepository;
    private final MandateRepository mandateRepository;
    private final RoleHistoryRepository roleHistoryRepository;
    private final MemberStatusHistoryRepository memberStatusHistoryRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    // ── SUBMIT (public, no auth) ──────────────────────────────

    @Transactional
    public MembershipRequestResponse submit(
            String email,
            String password,
            String firstName,
            String lastName,
            String motivationLetter,
            String requestedStatus,
            String establishment,
            String originLaboratory,
            MultipartFile cv) throws IOException {

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Un compte existe déjà avec cet email");
        }
        if (membershipRequestRepository.existsByEmailAndStatus(email, MembershipRequest.RequestStatus.PENDING)) {
            throw new IllegalArgumentException("Une demande est déjà en attente pour cet email");
        }

        Member.MemberStatus status;
        try {
            status = Member.MemberStatus.valueOf(requestedStatus.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Statut demandé invalide: " + requestedStatus);
        }

        String storedCvName = null;
        String originalCvName = null;
        if (cv != null && !cv.isEmpty()) {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            originalCvName = cv.getOriginalFilename();
            String extension = originalCvName != null && originalCvName.contains(".")
                    ? originalCvName.substring(originalCvName.lastIndexOf("."))
                    : "";
            storedCvName = UUID.randomUUID() + extension;
            Files.copy(cv.getInputStream(), uploadPath.resolve(storedCvName), StandardCopyOption.REPLACE_EXISTING);
        }

        MembershipRequest request = MembershipRequest.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .firstName(firstName)
                .lastName(lastName)
                .motivationLetter(motivationLetter)
                .requestedStatus(status)
                .establishment(establishment)
                .originLaboratory(originLaboratory)
                .cvPath(storedCvName)
                .cvOriginalFilename(originalCvName)
                .status(MembershipRequest.RequestStatus.PENDING)
                .submittedAt(LocalDateTime.now())
                .build();

        MembershipRequest saved = membershipRequestRepository.save(request);
        notificationService.notifyNewMembershipRequest(email);
        return toResponse(saved);
    }

    // ── LIST (DIRECTOR/ADMIN) ─────────────────────────────────

    public List<MembershipRequestResponse> getAll() {
        return membershipRequestRepository.findAllByOrderBySubmittedAtDesc().stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    public List<MembershipRequestResponse> getPending() {
        return membershipRequestRepository
                .findByStatusOrderBySubmittedAtDesc(MembershipRequest.RequestStatus.PENDING)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // ── ACCEPT — only the director currently in mandate (spec §6) ────

    @Transactional
    public MembershipRequestResponse accept(Long requestId) {
        requireActiveDirector();

        MembershipRequest request = membershipRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Demande introuvable"));

        if (request.getStatus() != MembershipRequest.RequestStatus.PENDING) {
            throw new IllegalArgumentException("Cette demande a déjà été traitée");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Un compte existe déjà avec cet email");
        }

        String actingDirector = currentUserEmail();
        LocalDate today = LocalDate.now();

        // 1. Create the User with the password the applicant chose at submission
        User user = User.builder()
                .email(request.getEmail())
                .password(request.getPassword())
                .role(User.UserRole.MEMBER)
                .status(User.UserStatus.ACTIVE)
                .build();
        userRepository.save(user);

        // 2. Create the Member profile
        Member member = Member.builder()
                .user(user)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .establishment(request.getEstablishment())
                .originLaboratory(request.getOriginLaboratory())
                .status(request.getRequestedStatus())
                .hireDate(today)
                .build();
        memberRepository.save(member);

        // 3. Initial affiliation
        affiliationRepository.save(Affiliation.builder()
                .member(member)
                .laboratory("LIAS")
                .startDate(today)
                .build());

        // 4. Seed role/status history (spec §7, §4/§8/§21)
        roleHistoryRepository.save(RoleHistory.builder()
                .user(user)
                .role(user.getRole())
                .startDate(today)
                .changedBy(actingDirector)
                .build());

        memberStatusHistoryRepository.save(MemberStatusHistory.builder()
                .member(member)
                .status(member.getStatus())
                .startDate(today)
                .changedBy(actingDirector)
                .build());

        // 5. Close out the request
        request.setStatus(MembershipRequest.RequestStatus.ACCEPTED);
        request.setDecidedAt(LocalDateTime.now());
        request.setDecidedBy(actingDirector);
        request.setCreatedUserId(user.getId());
        membershipRequestRepository.save(request);

        notificationService.notifyMembershipRequestAccepted(request.getEmail());

        return toResponse(request);
    }

    // ── REJECT — only the director currently in mandate ───────

    @Transactional
    public MembershipRequestResponse reject(Long requestId, String reason) {
        requireActiveDirector();

        MembershipRequest request = membershipRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Demande introuvable"));

        if (request.getStatus() != MembershipRequest.RequestStatus.PENDING) {
            throw new IllegalArgumentException("Cette demande a déjà été traitée");
        }

        request.setStatus(MembershipRequest.RequestStatus.REJECTED);
        request.setDecidedAt(LocalDateTime.now());
        request.setDecidedBy(currentUserEmail());
        request.setRejectionReason(reason);
        membershipRequestRepository.save(request);

        notificationService.notifyMembershipRequestRejected(request.getEmail(), reason);

        return toResponse(request);
    }

    // ── helpers ────────────────────────────────────────────────

    // Spec §6: "Gestion : Uniquement par le directeur en mandat" — this is
    // stricter than hasRole('DIRECTOR'): it checks there is a currently
    // active Mandate of role DIRECTOR for the acting user, since role and
    // mandate are spec-distinct (§21 "Séparation stricte : Statut / Rôle").
    private void requireActiveDirector() {
        String email = currentUserEmail();
        Member member = memberRepository.findByUserEmail(email)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        boolean isActiveDirector = mandateRepository.findActiveByMemberId(member.getId()).stream()
                .anyMatch(m -> m.getRole() == Mandate.MandateRole.DIRECTOR);

        if (!isActiveDirector) {
            throw new IllegalStateException(
                    "Seul le directeur en mandat actif peut accepter ou rejeter une demande d'adhésion");
        }
    }

    private String currentUserEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    private MembershipRequestResponse toResponse(MembershipRequest r) {
        MembershipRequestResponse dto = new MembershipRequestResponse();
        dto.setId(r.getId());
        dto.setEmail(r.getEmail());
        dto.setFirstName(r.getFirstName());
        dto.setLastName(r.getLastName());
        dto.setMotivationLetter(r.getMotivationLetter());
        dto.setCvOriginalFilename(r.getCvOriginalFilename());
        dto.setRequestedStatus(r.getRequestedStatus().name());
        dto.setEstablishment(r.getEstablishment());
        dto.setOriginLaboratory(r.getOriginLaboratory());
        dto.setStatus(r.getStatus().name());
        dto.setSubmittedAt(r.getSubmittedAt());
        dto.setDecidedAt(r.getDecidedAt());
        dto.setDecidedBy(r.getDecidedBy());
        dto.setRejectionReason(r.getRejectionReason());
        return dto;
    }
}
