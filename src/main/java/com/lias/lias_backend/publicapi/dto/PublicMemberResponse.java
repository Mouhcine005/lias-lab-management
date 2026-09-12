package com.lias.lias_backend.publicapi.dto;

import lombok.Data;

// Public-safe projection of a Member profile.
// Deliberately excludes: email, birthDate (spec §4.2 "confidentielle"), hireDate,
// and anything from User (status, role) — visitors only see presentation-level info.
@Data
public class PublicMemberResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String photoPath;
    private String biography;
    private String interests;
    private String establishment;
    private String status; // PERMANENT / ASSOCIATE / DOCTORAL — public per spec §2.1 "Équipes"
    private String team;
}
