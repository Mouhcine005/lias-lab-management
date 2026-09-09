package com.lias.lias_backend.publication.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PublicationResponse {
    private Long id;
    private String title;
    private String journal;
    private String conference;
    private String doi;
    private String url;
    private String authors;
    private String abstractText;
    private String team;
    private Integer year;
    private String type;
    private Long memberId;
    private String memberEmail;
    private String memberFirstName;
    private String memberLastName;
    private LocalDateTime createdAt;
}