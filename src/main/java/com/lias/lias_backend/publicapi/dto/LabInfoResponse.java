package com.lias.lias_backend.publicapi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LabInfoResponse {
    private String name;
    private String presentation;
    private LocalDate creationDate;
    private String faculty;
}
