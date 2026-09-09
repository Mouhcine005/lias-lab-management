package com.lias.lias_backend.publication.service;

import com.lias.lias_backend.member.entity.Member;
import com.lias.lias_backend.member.repository.MemberRepository;
import com.lias.lias_backend.publication.entity.Publication;
import com.lias.lias_backend.publication.dto.PublicationRequest;
import com.lias.lias_backend.publication.dto.PublicationResponse;
import com.lias.lias_backend.publication.repository.PublicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PublicationService {

    private final PublicationRepository publicationRepository;
    private final MemberRepository memberRepository;

    // Add a publication (current user)
    @Transactional
    public PublicationResponse addPublication(PublicationRequest request) {
        Member member = getCurrentMember();

        // auto-fill team from active affiliation if not provided
        String team = request.getTeam();

        Publication publication = Publication.builder()
                .member(member)
                .title(request.getTitle())
                .journal(request.getJournal())
                .conference(request.getConference())
                .doi(request.getDoi())
                .url(request.getUrl())
                .authors(request.getAuthors())
                .abstractText(request.getAbstractText())
                .team(team)
                .year(request.getYear())
                .type(request.getType())
                .build();

        return toResponse(publicationRepository.save(publication));
    }

    // Get all publications (public)
    public List<PublicationResponse> getAllPublications() {
        return publicationRepository.findAll()
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // Get publications by year
    public List<PublicationResponse> getByYear(Integer year) {
        return publicationRepository.findByYear(year)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // Get publications by team
    public List<PublicationResponse> getByTeam(String team) {
        return publicationRepository.findByTeam(team)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // Get my publications
    public List<PublicationResponse> getMyPublications() {
        Member member = getCurrentMember();
        return publicationRepository.findByMemberIdOrderByYearDesc(member.getId())
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // Get publications by member id
    public List<PublicationResponse> getByMember(Long memberId) {
        return publicationRepository.findByMemberIdOrderByYearDesc(memberId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // Update a publication (only owner)
    @Transactional
    public PublicationResponse updatePublication(Long id, PublicationRequest request) {
        Publication publication = publicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Publication not found"));

        Member member = getCurrentMember();
        if (!publication.getMember().getId().equals(member.getId()))
            throw new IllegalArgumentException("You can only edit your own publications");

        publication.setTitle(request.getTitle());
        publication.setJournal(request.getJournal());
        publication.setConference(request.getConference());
        publication.setDoi(request.getDoi());
        publication.setUrl(request.getUrl());
        publication.setAuthors(request.getAuthors());
        publication.setAbstractText(request.getAbstractText());
        publication.setTeam(request.getTeam());
        publication.setYear(request.getYear());
        publication.setType(request.getType());

        return toResponse(publicationRepository.save(publication));
    }

    // Delete a publication (only owner or admin)
    @Transactional
    public void deletePublication(Long id) {
        Publication publication = publicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Publication not found"));

        Member member = getCurrentMember();
        if (!publication.getMember().getId().equals(member.getId()))
            throw new IllegalArgumentException("You can only delete your own publications");

        publicationRepository.delete(publication);
    }

    // --- helpers ---

    private Member getCurrentMember() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return memberRepository.findByUserEmail(email)
                .orElseThrow(() -> new RuntimeException("Member not found"));
    }

    private PublicationResponse toResponse(Publication p) {
        PublicationResponse r = new PublicationResponse();
        r.setId(p.getId());
        r.setTitle(p.getTitle());
        r.setJournal(p.getJournal());
        r.setConference(p.getConference());
        r.setDoi(p.getDoi());
        r.setUrl(p.getUrl());
        r.setAuthors(p.getAuthors());
        r.setAbstractText(p.getAbstractText());
        r.setTeam(p.getTeam());
        r.setYear(p.getYear());
        r.setType(p.getType().name());
        r.setMemberId(p.getMember().getId());
        r.setMemberEmail(p.getMember().getUser().getEmail());
        r.setMemberFirstName(p.getMember().getFirstName());
        r.setMemberLastName(p.getMember().getLastName());
        r.setCreatedAt(p.getCreatedAt());
        return r;
    }
}