package com.lias.lias_backend.membershiprequest.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MembershipRequestResponse {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String motivationLetter;
    private String cvOriginalFilename;
    private String requestedStatus;
    private String establishment;
    private String originLaboratory;
    private String status;
    private LocalDateTime submittedAt;
    private LocalDateTime decidedAt;
    private String decidedBy;
    private String rejectionReason;
}
