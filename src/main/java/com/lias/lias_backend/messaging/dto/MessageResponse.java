package com.lias.lias_backend.messaging.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MessageResponse {
    private Long id;
    private Long threadId;
    private String senderName;
    private String content;
    private LocalDateTime sentAt;
}
