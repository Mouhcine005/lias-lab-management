package com.lias.lias_backend.governance.service;

import com.lias.lias_backend.governance.dto.MandateRequest;
import com.lias.lias_backend.governance.dto.MandateResponse;
import com.lias.lias_backend.governance.entity.Mandate;
import com.lias.lias_backend.governance.repository.MandateRepository;
import com.lias.lias_backend.member.entity.Member;
import com.lias.lias_backend.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MandateService {

    private final MandateRepository mandateRepository;
    private final MemberRepository memberRepository;

    // Get all mandates
    public List<MandateResponse> getAllMandates() {
        return mandateRepository.findAll()
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // Get all active mandates
    public List<MandateResponse> getActiveMandates() {
        return mandateRepository.findAllActive()
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // Get current director
    public MandateResponse getCurrentDirector() {
        return mandateRepository.findActiveByRole(Mandate.MandateRole.DIRECTOR)
                .map(this::toResponse)
                .orElseThrow(() -> new RuntimeException("No active director found"));
    }

    // Get current vice-director
    public MandateResponse getCurrentViceDirector() {
        return mandateRepository.findActiveByRole(Mandate.MandateRole.VICE_DIRECTOR)
                .map(this::toResponse)
                .orElseThrow(() -> new RuntimeException("No active vice-director found"));
    }

    // Get mandates by member
    public List<MandateResponse> getByMember(Long memberId) {
        return mandateRepository.findByMemberId(memberId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // Get mandates by role
    public List<MandateResponse> getByRole(Mandate.MandateRole role) {
        return mandateRepository.findByRole(role)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // Create a mandate (ADMIN only)
    @Transactional
    public MandateResponse createMandate(MandateRequest request) {
        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new RuntimeException("Member not found"));

        // Close existing active mandate for same role if exists
        mandateRepository.findActiveByRole(request.getRole())
                .ifPresent(existing -> {
                    existing.setEndDate(request.getStartDate().minusDays(1));
                    mandateRepository.save(existing);
                });

        Mandate mandate = Mandate.builder()
                .member(member)
                .role(request.getRole())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .team(request.getTeam())
                .build();

        return toResponse(mandateRepository.save(mandate));
    }

    // End a mandate
    @Transactional
    public MandateResponse endMandate(Long id) {
        Mandate mandate = mandateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mandate not found"));
        mandate.setEndDate(LocalDate.now());
        return toResponse(mandateRepository.save(mandate));
    }

    // Delete a mandate
    @Transactional
    public void deleteMandate(Long id) {
        if (!mandateRepository.existsById(id))
            throw new RuntimeException("Mandate not found");
        mandateRepository.deleteById(id);
    }

    // --- helper ---

    private MandateResponse toResponse(Mandate m) {
        MandateResponse r = new MandateResponse();
        r.setId(m.getId());
        r.setMemberId(m.getMember().getId());
        r.setMemberFirstName(m.getMember().getFirstName());
        r.setMemberLastName(m.getMember().getLastName());
        r.setMemberEmail(m.getMember().getUser().getEmail());
        r.setRole(m.getRole().name());
        r.setStartDate(m.getStartDate());
        r.setEndDate(m.getEndDate());
        r.setTeam(m.getTeam());
        r.setActive(m.isActive());
        r.setCreatedAt(m.getCreatedAt());
        return r;
    }
}