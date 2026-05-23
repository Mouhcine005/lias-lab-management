package com.lias.lias_backend.member.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDate;

@Data
public class UpdateProfileRequest {

    private String firstName;
    private String lastName;

    @Size(max = 1000, message = "Biography cannot exceed 1000 characters")
    private String biography;

    @Size(max = 500, message = "Interests cannot exceed 500 characters")
    private String interests;

    private String establishment;
    private String originLaboratory;
    private LocalDate birthDate;
}