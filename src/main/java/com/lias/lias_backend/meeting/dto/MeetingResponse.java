package com.lias.lias_backend.meeting.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class MeetingResponse {
    private Long id;
    private String title;
    private String description;
    private String location;
    private String agenda;
    private LocalDate date;
    private String status;
    private Long createdById;
    private String createdByEmail;
    private String createdByFirstName;
    private String createdByLastName;
    private String pvFileName;
    private String pvDownloadUrl;
    private LocalDateTime createdAt;
}