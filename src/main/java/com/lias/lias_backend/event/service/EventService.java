package com.lias.lias_backend.event.service;

import com.lias.lias_backend.event.dto.EventRequest;
import com.lias.lias_backend.event.dto.EventResponse;
import com.lias.lias_backend.event.entity.Event;
import com.lias.lias_backend.event.repository.EventRepository;
import com.lias.lias_backend.member.entity.Member;
import com.lias.lias_backend.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final MemberRepository memberRepository;

    // Get all events
    public List<EventResponse> getAllEvents() {
        return eventRepository.findAll()
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // Get event by id
    public EventResponse getEvent(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        return toResponse(event);
    }

    // Get by type
    public List<EventResponse> getByType(Event.EventType type) {
        return eventRepository.findByType(type)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // Get by status
    public List<EventResponse> getByStatus(Event.EventStatus status) {
        return eventRepository.findByStatus(status)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // Get events organized by current user
    public List<EventResponse> getMyEvents() {
        Member member = getCurrentMember();
        return eventRepository.findByOrganizerId(member.getId())
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // Create event (ADMIN, DIRECTOR only)
    @Transactional
    public EventResponse createEvent(EventRequest request) {
        Member organizer = getCurrentMember();

        Event event = Event.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .location(request.getLocation())
                .edition(request.getEdition())
                .website(request.getWebsite())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .type(request.getType())
                .status(request.getStatus() != null ? request.getStatus() : Event.EventStatus.PLANNED)
                .organizer(organizer)
                .build();

        return toResponse(eventRepository.save(event));
    }

    // Update event
    @Transactional
    public EventResponse updateEvent(Long id, EventRequest request) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setLocation(request.getLocation());
        event.setEdition(request.getEdition());
        event.setWebsite(request.getWebsite());
        event.setStartDate(request.getStartDate());
        event.setEndDate(request.getEndDate());
        event.setType(request.getType());
        if (request.getStatus() != null) event.setStatus(request.getStatus());

        return toResponse(eventRepository.save(event));
    }

    // Update event status only
    @Transactional
    public EventResponse updateStatus(Long id, Event.EventStatus status) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        event.setStatus(status);
        return toResponse(eventRepository.save(event));
    }

    // Delete event
    @Transactional
    public void deleteEvent(Long id) {
        if (!eventRepository.existsById(id))
            throw new RuntimeException("Event not found");
        eventRepository.deleteById(id);
    }

    // --- helpers ---

    private Member getCurrentMember() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return memberRepository.findByUserEmail(email)
                .orElseThrow(() -> new RuntimeException("Member not found"));
    }

    private EventResponse toResponse(Event e) {
        EventResponse r = new EventResponse();
        r.setId(e.getId());
        r.setTitle(e.getTitle());
        r.setDescription(e.getDescription());
        r.setLocation(e.getLocation());
        r.setEdition(e.getEdition());
        r.setWebsite(e.getWebsite());
        r.setStartDate(e.getStartDate());
        r.setEndDate(e.getEndDate());
        r.setType(e.getType().name());
        r.setStatus(e.getStatus() != null ? e.getStatus().name() : null);
        r.setCreatedAt(e.getCreatedAt());

        if (e.getOrganizer() != null) {
            r.setOrganizerId(e.getOrganizer().getId());
            r.setOrganizerFirstName(e.getOrganizer().getFirstName());
            r.setOrganizerLastName(e.getOrganizer().getLastName());
            r.setOrganizerEmail(e.getOrganizer().getUser().getEmail());
        }

        return r;
    }
}