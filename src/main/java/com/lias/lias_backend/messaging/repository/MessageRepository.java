package com.lias.lias_backend.messaging.repository;

import com.lias.lias_backend.messaging.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByThreadIdOrderByCreatedAtAsc(Long threadId);
}
