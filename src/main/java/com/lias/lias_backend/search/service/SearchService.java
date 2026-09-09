package com.lias.lias_backend.search.service;

import com.lias.lias_backend.document.repository.DocumentRepository;
import com.lias.lias_backend.event.repository.EventRepository;
import com.lias.lias_backend.member.repository.MemberRepository;
import com.lias.lias_backend.publication.repository.PublicationRepository;
import com.lias.lias_backend.search.dto.SearchResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final MemberRepository memberRepository;
    private final DocumentRepository documentRepository;
    private final EventRepository eventRepository;
    private final PublicationRepository publicationRepository;

    public List<SearchResult> search(String query) {
        if (query == null || query.isBlank()) return List.of();
        String q = query.trim().toLowerCase();
        List<SearchResult> results = new ArrayList<>();

        // Members — search first name, last name, and email
        memberRepository.findAll().stream()
                .filter(m -> {
                    String fullName = ((m.getFirstName() == null ? "" : m.getFirstName()) + " "
                            + (m.getLastName() == null ? "" : m.getLastName())).toLowerCase();
                    String email = m.getUser() != null && m.getUser().getEmail() != null
                            ? m.getUser().getEmail().toLowerCase() : "";
                    return fullName.contains(q) || email.contains(q);
                })
                .forEach(m -> results.add(new SearchResult(
                        "MEMBER", m.getId(),
                        m.getFirstName() + " " + m.getLastName(),
                        m.getUser() != null ? m.getUser().getEmail() : "")));

        // Documents — search fileName and description (Document has no `title` field)
        documentRepository.findAll().stream()
                .filter(d -> (d.getFileName() != null && d.getFileName().toLowerCase().contains(q))
                        || (d.getDescription() != null && d.getDescription().toLowerCase().contains(q)))
                .forEach(d -> results.add(new SearchResult(
                        "DOCUMENT", d.getId(),
                        d.getFileName(),
                        d.getType() != null ? d.getType().name() : "")));

        // Events — search title and description
        eventRepository.findAll().stream()
                .filter(e -> (e.getTitle() != null && e.getTitle().toLowerCase().contains(q))
                        || (e.getDescription() != null && e.getDescription().toLowerCase().contains(q)))
                .forEach(e -> results.add(new SearchResult(
                        "EVENT", e.getId(),
                        e.getTitle(),
                        e.getStartDate() != null ? e.getStartDate().toString() : "")));

        // Publications — search title and authors
        publicationRepository.findAll().stream()
                .filter(p -> (p.getTitle() != null && p.getTitle().toLowerCase().contains(q))
                        || (p.getAuthors() != null && p.getAuthors().toLowerCase().contains(q)))
                .forEach(p -> results.add(new SearchResult(
                        "PUBLICATION", p.getId(),
                        p.getTitle(),
                        p.getYear() != null ? p.getYear().toString() : "")));

        return results;
    }
}
