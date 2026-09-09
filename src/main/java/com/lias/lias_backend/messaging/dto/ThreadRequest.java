package com.lias.lias_backend.messaging.dto;

import lombok.Data;
import java.util.List;

@Data
public class ThreadRequest {
    private String title;
    private String type; // GENERAL, TEAM, EVENT
    private String team;
    private Long eventId;
    private List<Long> participantIds;
}
