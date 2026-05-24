package com.lias.lias_backend.notification.service;

import com.lias.lias_backend.member.entity.Member;
import com.lias.lias_backend.member.entity.User;
import com.lias.lias_backend.member.repository.MemberRepository;
import com.lias.lias_backend.member.repository.UserRepository;
import com.lias.lias_backend.notification.dto.NotificationResponse;
import com.lias.lias_backend.notification.entity.Notification;
import com.lias.lias_backend.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final MemberRepository memberRepository;
    private final UserRepository userRepository;
    private final JavaMailSender mailSender;

    // ── GET MY NOTIFICATIONS ──────────────────────────────────

    public List<NotificationResponse> getMyNotifications() {
        Member member = getCurrentMember();
        return notificationRepository
                .findByMemberIdOrderByCreatedAtDesc(member.getId())
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<NotificationResponse> getMyUnread() {
        Member member = getCurrentMember();
        return notificationRepository
                .findByMemberIdAndReadFalse(member.getId())
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public long getUnreadCount() {
        Member member = getCurrentMember();
        return notificationRepository.countByMemberIdAndReadFalse(member.getId());
    }

    @Transactional
    public void markAsRead(Long id) {
        Notification n = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        n.setRead(true);
        notificationRepository.save(n);
    }

    @Transactional
    public void markAllAsRead() {
        Member member = getCurrentMember();
        List<Notification> unread = notificationRepository
                .findByMemberIdAndReadFalse(member.getId());
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }

    // ── NOTIFICATION TRIGGERS ─────────────────────────────────

    // Called on register — notify all ADMINs and DIRECTORs
    @Transactional
    public void notifyNewMemberPending(String newMemberEmail) {
        List<Member> admins = memberRepository.findAll().stream()
                .filter(m -> m.getUser().getRole() == User.UserRole.ADMIN
                        || m.getUser().getRole() == User.UserRole.DIRECTOR)
                .filter(m -> m.getUser().getStatus() == User.UserStatus.ACTIVE)
                .collect(Collectors.toList());

        String title = "New Member Pending Approval";
        String message = "A new member has registered and is waiting for approval: " + newMemberEmail;

        for (Member admin : admins) {
            saveNotification(admin, title, message, Notification.NotificationType.NEW_MEMBER_PENDING);
            sendEmail(admin.getUser().getEmail(), title, message);
        }
    }

    // Called on approve — notify the member
    @Transactional
    public void notifyMemberApproved(Member member) {
        String title = "Account Approved";
        String message = "Your LIAS account has been approved. You can now log in and access all features.";
        saveNotification(member, title, message, Notification.NotificationType.ACCOUNT_APPROVED);
        sendEmail(member.getUser().getEmail(), title, message);
    }

    // Called on reject — notify the member
    @Transactional
    public void notifyMemberRejected(Member member) {
        String title = "Account Rejected";
        String message = "Your LIAS account request has been rejected. Please contact the laboratory director for more information.";
        saveNotification(member, title, message, Notification.NotificationType.ACCOUNT_REJECTED);
        sendEmail(member.getUser().getEmail(), title, message);
    }

    // Called on new event — notify all active members
    @Transactional
    public void notifyNewEvent(String eventTitle) {
        String title = "New Event: " + eventTitle;
        String message = "A new event has been added to the LIAS calendar: " + eventTitle;
        notifyAllActiveMembers(title, message, Notification.NotificationType.NEW_EVENT);
    }

    // Called on new document — notify all active members
    @Transactional
    public void notifyNewDocument(String fileName, String eventTitle) {
        String title = "New Document Uploaded";
        String message = eventTitle != null
                ? "A new document \"" + fileName + "\" has been uploaded for event: " + eventTitle
                : "A new document \"" + fileName + "\" has been uploaded.";
        notifyAllActiveMembers(title, message, Notification.NotificationType.NEW_DOCUMENT);
    }

    // Called on new meeting — notify all active members
    @Transactional
    public void notifyNewMeeting(String meetingTitle, String date) {
        String title = "New Meeting Scheduled";
        String message = "A new meeting \"" + meetingTitle + "\" has been scheduled for " + date;
        notifyAllActiveMembers(title, message, Notification.NotificationType.NEW_MEETING);
    }

    // ── HELPERS ───────────────────────────────────────────────

    private void notifyAllActiveMembers(String title, String message,
                                        Notification.NotificationType type) {
        List<Member> activeMembers = memberRepository.findAll().stream()
                .filter(m -> m.getUser().getStatus() == User.UserStatus.ACTIVE)
                .collect(Collectors.toList());

        for (Member member : activeMembers) {
            saveNotification(member, title, message, type);
            sendEmail(member.getUser().getEmail(), title, message);
        }
    }

    private void saveNotification(Member member, String title, String message,
                                  Notification.NotificationType type) {
        Notification notification = Notification.builder()
                .member(member)
                .title(title)
                .message(message)
                .type(type)
                .read(false)
                .build();
        notificationRepository.save(notification);
    }

    private void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setTo(to);
            mail.setSubject("[LIAS] " + subject);
            mail.setText(body + "\n\n---\nLIAS Laboratory Management System");
            mailSender.send(mail);
        } catch (Exception e) {
            // Log but don't fail the main operation if email fails
            log.warn("Failed to send email to {}: {}", to, e.getMessage());
        }
    }

    private Member getCurrentMember() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return memberRepository.findByUserEmail(email)
                .orElseThrow(() -> new RuntimeException("Member not found"));
    }

    private NotificationResponse toResponse(Notification n) {
        NotificationResponse r = new NotificationResponse();
        r.setId(n.getId());
        r.setTitle(n.getTitle());
        r.setMessage(n.getMessage());
        r.setType(n.getType().name());
        r.setRead(n.isRead());
        r.setCreatedAt(n.getCreatedAt());
        return r;
    }
}