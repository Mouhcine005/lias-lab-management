package com.lias.lias_backend.member.service;

import com.lias.lias_backend.member.dto.*;
import com.lias.lias_backend.member.entity.*;
import com.lias.lias_backend.member.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final AffiliationRepository affiliationRepository;

    public MemberProfileResponse getProfile(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));
        return toResponse(member);
    }

    public MemberProfileResponse getMyProfile() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        Member member = memberRepository.findByUserEmail(email)
                .orElseThrow(() -> new RuntimeException("Member not found"));
        return toResponse(member);
    }

    @Transactional
    public MemberProfileResponse updateMyProfile(UpdateProfileRequest request) {
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        Member member = memberRepository.findByUserEmail(email)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        if (request.getFirstName() != null) member.setFirstName(request.getFirstName());
        if (request.getLastName() != null) member.setLastName(request.getLastName());
        if (request.getBiography() != null) member.setBiography(request.getBiography());
        if (request.getInterests() != null) member.setInterests(request.getInterests());
        if (request.getEstablishment() != null) member.setEstablishment(request.getEstablishment());
        if (request.getOriginLaboratory() != null) member.setOriginLaboratory(request.getOriginLaboratory());

        memberRepository.save(member);
        return toResponse(member);
    }

    private MemberProfileResponse toResponse(Member member) {
        MemberProfileResponse response = new MemberProfileResponse();
        response.setId(member.getId());
        response.setEmail(member.getUser().getEmail());
        response.setFirstName(member.getFirstName());
        response.setLastName(member.getLastName());
        response.setPhotoPath(member.getPhotoPath());
        response.setBiography(member.getBiography());
        response.setInterests(member.getInterests());
        response.setEstablishment(member.getEstablishment());
        response.setOriginLaboratory(member.getOriginLaboratory());
        response.setHireDate(member.getHireDate());
        response.setStatus(member.getStatus());
        response.setRole(member.getUser().getRole().name());

        // attach active affiliation
        affiliationRepository.findByMemberIdAndEndDateIsNull(member.getId())
                .ifPresent(a -> {
                    response.setCurrentTeam(a.getTeam());
                    response.setCurrentLaboratory(a.getLaboratory());
                });

        return response;
    }
}