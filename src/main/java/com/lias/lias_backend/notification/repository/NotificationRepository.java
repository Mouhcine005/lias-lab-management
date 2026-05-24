package com.lias.lias_backend.notification.repository;

import com.lias.lias_backend.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByMemberIdOrderByCreatedAtDesc(Long memberId);
    List<Notification> findByMemberIdAndReadFalse(Long memberId);
    long countByMemberIdAndReadFalse(Long memberId);
}