package com.lias.lias_backend.publicapi.service;

import com.lias.lias_backend.event.entity.Event;
import com.lias.lias_backend.event.repository.EventRepository;
import com.lias.lias_backend.member.entity.Member;
import com.lias.lias_backend.member.repository.AffiliationRepository;
import com.lias.lias_backend.member.repository.MemberRepository;
import com.lias.lias_backend.publication.entity.Publication;
import com.lias.lias_backend.publication.repository.PublicationRepository;
import com.lias.lias_backend.publicapi.dto.LabInfoResponse;
import com.lias.lias_backend.publicapi.dto.PublicEventResponse;
import com.lias.lias_backend.publicapi.dto.PublicMemberResponse;
import com.lias.lias_backend.publicapi.dto.PublicPublicationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PublicService {

    private final MemberRepository memberRepository;
    private final AffiliationRepository affiliationRepository;
    private final EventRepository eventRepository;
    private final PublicationRepository publicationRepository;

    // Static for now — move to a DB-backed "LabSettings" entity later if the
    // director needs to edit this from the admin UI without a redeploy.
    public LabInfoResponse getLabInfo() {
        return new LabInfoResponse(
                "LIAS",
                "Laboratoire d'Informatique et d'Aide à la décision, Faculté des Sciences Ben M'Sik, Université Hassan II de Casablanca.",
                LocalDate.of(2012, 1, 1),
                "Faculté des Sciences Ben M'Sik"
        );
    }

    // Only permanent, associate, and doctoral members are shown publicly.
    // Retired/former members are excluded from the public "teams" page even
    // though their history is preserved internally (spec §8).
    public List<PublicMemberResponse> getPublicTeams() {
        List<Member> members = memberRepository.findAll().stream()
                .filter(m -> m.getStatus() == Member.MemberStatus.PERMANENT
                        || m.getStatus() == Member.MemberStatus.ASSOCIATE
                        || m.getStatus() == Member.MemberStatus.DOCTORAL)
                .collect(Collectors.toList());

        return members.stream().map(this::toPublicMember).collect(Collectors.toList());
    }

    public List<PublicEventResponse> getPublicEvents() {
        return eventRepository.findAll().stream()
                .map(this::toPublicEvent)
                .collect(Collectors.toList());
    }

    public List<PublicPublicationResponse> getPublicPublications() {
        return publicationRepository.findAll().stream()
                .map(this::toPublicPublication)
                .collect(Collectors.toList());
    }

    // --- mapping helpers ---

    private PublicMemberResponse toPublicMember(Member member) {
        PublicMemberResponse r = new PublicMemberResponse();
        r.setId(member.getId());
        r.setFirstName(member.getFirstName());
        r.setLastName(member.getLastName());
        r.setPhotoPath(member.getPhotoPath());
        r.setBiography(member.getBiography());
        r.setInterests(member.getInterests());
        r.setEstablishment(member.getEstablishment());
        r.setStatus(member.getStatus() != null ? member.getStatus().name() : null);

        affiliationRepository.findActiveByMemberId(member.getId())
                .ifPresent(a -> r.setTeam(a.getTeam()));

        return r;
    }

    private PublicEventResponse toPublicEvent(Event event) {
        PublicEventResponse r = new PublicEventResponse();
        r.setId(event.getId());
        r.setTitle(event.getTitle());
        r.setDescription(event.getDescription());
        r.setLocation(event.getLocation());
        r.setEdition(event.getEdition());
        r.setWebsite(event.getWebsite());
        r.setStartDate(event.getStartDate());
        r.setEndDate(event.getEndDate());
        r.setType(event.getType() != null ? event.getType().name() : null);
        r.setStatus(event.getStatus() != null ? event.getStatus().name() : null);
        return r;
    }

    private PublicPublicationResponse toPublicPublication(Publication pub) {
        PublicPublicationResponse r = new PublicPublicationResponse();
        r.setId(pub.getId());
        r.setTitle(pub.getTitle());
        r.setJournal(pub.getJournal());
        r.setConference(pub.getConference());
        r.setDoi(pub.getDoi());
        r.setUrl(pub.getUrl());
        r.setAuthors(pub.getAuthors());
        r.setTeam(pub.getTeam());
        r.setYear(pub.getYear());
        r.setType(pub.getType() != null ? pub.getType().name() : null);
        if (pub.getMember() != null) {
            r.setMemberFirstName(pub.getMember().getFirstName());
            r.setMemberLastName(pub.getMember().getLastName());
        }
        return r;
    }
}
