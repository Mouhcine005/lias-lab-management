package com.lias.lias_backend.event.repository;

import com.lias.lias_backend.event.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByType(Event.EventType type);
    List<Event> findByStatus(Event.EventStatus status);
    List<Event> findByOrganizerId(Long organizerId);
}