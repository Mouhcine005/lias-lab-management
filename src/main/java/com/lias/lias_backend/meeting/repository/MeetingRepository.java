package com.lias.lias_backend.meeting.repository;

import com.lias.lias_backend.meeting.entity.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface MeetingRepository extends JpaRepository<Meeting, Long> {
    List<Meeting> findByStatus(Meeting.MeetingStatus status);
    List<Meeting> findByCreatedById(Long memberId);
    List<Meeting> findByOrderByDateDesc();

    @Query("SELECT m FROM Meeting m WHERE m.date BETWEEN :from AND :to")
    List<Meeting> findMeetingsBetween(@Param("from") LocalDate from, @Param("to") LocalDate to);
}