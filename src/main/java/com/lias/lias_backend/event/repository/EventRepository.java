package com.lias.lias_backend.event.repository;

import com.lias.lias_backend.event.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByType(Event.EventType type);
    List<Event> findByStatus(Event.EventStatus status);
    List<Event> findByOrganizerId(Long organizerId);

    @Query("SELECT e FROM Event e WHERE e.startDate BETWEEN :from AND :to")
    List<Event> findEventsBetween(@Param("from") LocalDate from, @Param("to") LocalDate to);
}