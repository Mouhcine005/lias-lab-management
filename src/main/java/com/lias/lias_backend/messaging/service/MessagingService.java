package com.lias.lias_backend.messaging.service;

import com.lias.lias_backend.member.entity.Member;
import com.lias.lias_backend.member.repository.MemberRepository;
import com.lias.lias_backend.messaging.dto.*;
import com.lias.lias_backend.messaging.entity.Message;
import com.lias.lias_backend.messaging.entity.MessageThread;
import com.lias.lias_backend.messaging.repository.MessageRepository;
import com.lias.lias_backend.messaging.repository.MessageThreadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessagingService {

    private final MessageThreadRepository threadRepository;
    private final MessageRepository messageRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public ThreadResponse createThread(ThreadRequest req) {
        Member me = getCurrentMember();
        List<Member> participants = memberRepository.findAllById(req.getParticipantIds());
        if (!participants.contains(me)) participants.add(me);

        MessageThread thread = MessageThread.builder()
                .title(req.getTitle())
                .type(MessageThread.ThreadType.valueOf(req.getType().toUpperCase()))
                .team(req.getTeam())
                .eventId(req.getEventId())
                .participants(participants)
                .build();

        return toThreadResponse(threadRepository.save(thread));
    }

    public List<ThreadResponse> getMyThreads() {
        Member me = getCurrentMember();
        return threadRepository.findByParticipantId(me.getId())
                .stream().map(this::toThreadResponse).collect(Collectors.toList());
    }

    @Transactional
    public MessageResponse sendMessage(Long threadId, MessageRequest req) {
        Member me = getCurrentMember();
        MessageThread thread = threadRepository.findById(threadId)
                .orElseThrow(() -> new RuntimeException("Thread not found"));

        Message msg = Message.builder()
                .thread(thread)
                .sender(me)
                .content(req.getContent())
                .build();

        return toMessageResponse(messageRepository.save(msg));
    }

    public List<MessageResponse> getMessages(Long threadId) {
        return messageRepository.findByThreadIdOrderByCreatedAtAsc(threadId)
                .stream().map(this::toMessageResponse).collect(Collectors.toList());
    }

    // --- helpers ---

    private Member getCurrentMember() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return memberRepository.findByUserEmail(email)
                .orElseThrow(() -> new RuntimeException("Member not found"));
    }

    private ThreadResponse toThreadResponse(MessageThread t) {
        ThreadResponse r = new ThreadResponse();
        r.setId(t.getId());
        r.setTitle(t.getTitle());
        r.setType(t.getType().name());
        r.setTeam(t.getTeam());
        r.setEventId(t.getEventId());
        r.setParticipantNames(t.getParticipants().stream()
                .map(m -> m.getFirstName() + " " + m.getLastName())
                .collect(Collectors.toList()));
        r.setCreatedAt(t.getCreatedAt());
        return r;
    }

    private MessageResponse toMessageResponse(Message m) {
        MessageResponse r = new MessageResponse();
        r.setId(m.getId());
        r.setThreadId(m.getThread().getId());
        r.setSenderName(m.getSender().getFirstName() + " " + m.getSender().getLastName());
        r.setContent(m.getContent());
        r.setSentAt(m.getCreatedAt());
        return r;
    }
}
