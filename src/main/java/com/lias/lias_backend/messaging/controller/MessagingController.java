package com.lias.lias_backend.messaging.controller;

import com.lias.lias_backend.messaging.dto.*;
import com.lias.lias_backend.messaging.service.MessagingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messaging")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('MEMBER', 'DIRECTOR', 'ADMIN')")
public class MessagingController {

    private final MessagingService messagingService;

    @PostMapping("/threads")
    public ResponseEntity<ThreadResponse> createThread(@RequestBody ThreadRequest req) {
        return ResponseEntity.ok(messagingService.createThread(req));
    }

    @GetMapping("/threads")
    public ResponseEntity<List<ThreadResponse>> getMyThreads() {
        return ResponseEntity.ok(messagingService.getMyThreads());
    }

    @PostMapping("/threads/{threadId}/messages")
    public ResponseEntity<MessageResponse> sendMessage(
            @PathVariable Long threadId,
            @RequestBody MessageRequest req) {
        return ResponseEntity.ok(messagingService.sendMessage(threadId, req));
    }

    @GetMapping("/threads/{threadId}/messages")
    public ResponseEntity<List<MessageResponse>> getMessages(@PathVariable Long threadId) {
        return ResponseEntity.ok(messagingService.getMessages(threadId));
    }
}
