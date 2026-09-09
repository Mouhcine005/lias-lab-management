package com.lias.lias_backend.member.dto;

import com.lias.lias_backend.member.entity.Member;
import lombok.Data;
import java.time.LocalDate;

@Data
public class MemberProfileResponse {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String photoPath;
    private String biography;
    private String interests;
    private String establishment;
    private String originLaboratory;
    private LocalDate hireDate;
    private Member.MemberStatus status;
    private String role;

    // active affiliation info
    private String currentTeam;
    private String currentLaboratory;
}