package com.lias.lias_backend.meeting.dto;

import com.lias.lias_backend.meeting.entity.Meeting;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class MeetingRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;
    private String location;
    private String agenda;

    @NotNull(message = "Date is required")
    private LocalDate date;

    private Meeting.MeetingStatus status;
}