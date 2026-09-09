package com.lias.lias_backend.member.service;

import com.lias.lias_backend.member.dto.AffiliationRequest;
import com.lias.lias_backend.member.dto.AffiliationResponse;
import com.lias.lias_backend.member.entity.Affiliation;
import com.lias.lias_backend.member.entity.Member;
import com.lias.lias_backend.member.repository.AffiliationRepository;
import com.lias.lias_backend.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AffiliationService {

    private final AffiliationRepository affiliationRepository;
    private final MemberRepository memberRepository;

    // Get full affiliation history for current user
    public List<AffiliationResponse> getMyAffiliations() {
        Member member = getCurrentMember();
        return affiliationRepository.findByMemberId(member.getId())
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // Get full affiliation history for any member (admin/director)
    public List<AffiliationResponse> getAffiliations(Long memberId) {
        if (!memberRepository.existsById(memberId))
            throw new RuntimeException("Member not found");
        return affiliationRepository.findByMemberId(memberId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // Add a new affiliation (closes current active one first)
    @Transactional
    public AffiliationResponse addAffiliation(Long memberId, AffiliationRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        // Close active affiliation if one exists
        affiliationRepository.findByMemberIdAndEndDateIsNull(memberId)
                .ifPresent(active -> {
                    active.setEndDate(request.getStartDate().minusDays(1));
                    affiliationRepository.save(active);
                });

        Affiliation affiliation = Affiliation.builder()
                .member(member)
                .laboratory(request.getLaboratory())
                .team(request.getTeam())
                .startDate(request.getStartDate())
                .build();

        return toResponse(affiliationRepository.save(affiliation));
    }

    // Close current active affiliation (member leaving)
    @Transactional
    public AffiliationResponse closeAffiliation(Long memberId) {
        Affiliation active = affiliationRepository.findByMemberIdAndEndDateIsNull(memberId)
                .orElseThrow(() -> new RuntimeException("No active affiliation found for this member"));

        active.setEndDate(LocalDate.now());
        return toResponse(affiliationRepository.save(active));
    }

    // --- helpers ---

    private Member getCurrentMember() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return memberRepository.findByUserEmail(email)
                .orElseThrow(() -> new RuntimeException("Member not found"));
    }

    private AffiliationResponse toResponse(Affiliation a) {
        AffiliationResponse r = new AffiliationResponse();
        r.setId(a.getId());
        r.setLaboratory(a.getLaboratory());
        r.setTeam(a.getTeam());
        r.setStartDate(a.getStartDate());
        r.setEndDate(a.getEndDate());
        r.setActive(a.isActive());
        return r;
    }
}