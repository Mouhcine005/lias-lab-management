package com.lias.lias_backend.publicapi.dto;

import lombok.Data;

// Public-safe projection of a Publication. Excludes memberEmail — visitors
// see author names but not contact info.
@Data
public class PublicPublicationResponse {
    private Long id;
    private String title;
    private String journal;
    private String conference;
    private String doi;
    private String url;
    private String authors;
    private String team;
    private Integer year;
    private String type;
    private String memberFirstName;
    private String memberLastName;
}
