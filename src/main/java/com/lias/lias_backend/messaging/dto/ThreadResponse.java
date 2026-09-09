package com.lias.lias_backend.messaging.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ThreadResponse {
    private Long id;
    private String title;
    private String type;
    private String team;
    private Long eventId;
    private List<String> participantNames;
    private LocalDateTime createdAt;
}
