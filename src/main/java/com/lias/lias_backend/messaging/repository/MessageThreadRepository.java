package com.lias.lias_backend.messaging.repository;

import com.lias.lias_backend.messaging.entity.MessageThread;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MessageThreadRepository extends JpaRepository<MessageThread, Long> {

    @Query("SELECT t FROM MessageThread t JOIN t.participants p WHERE p.id = :memberId")
    List<MessageThread> findByParticipantId(Long memberId);

    List<MessageThread> findByTeam(String team);
    List<MessageThread> findByEventId(Long eventId);
}
