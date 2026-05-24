package com.lias.lias_backend.meeting.repository;

import com.lias.lias_backend.meeting.entity.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MeetingRepository extends JpaRepository<Meeting, Long> {
    List<Meeting> findByStatus(Meeting.MeetingStatus status);
    List<Meeting> findByCreatedById(Long memberId);
    List<Meeting> findByOrderByDateDesc();
}