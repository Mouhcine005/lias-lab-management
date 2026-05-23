package com.lias.lias_backend.member.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class AdminMemberResponse {
    private Long userId;
    private Long memberId;
    private String email;
    private String firstName;
    private String lastName;
    private String userStatus;
    private String userRole;
    private String memberStatus;
    private LocalDate hireDate;
    private String currentLaboratory;
    private String currentTeam;
}