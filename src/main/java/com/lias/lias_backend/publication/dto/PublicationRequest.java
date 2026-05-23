package com.lias.lias_backend.publication.dto;

import com.lias.lias_backend.publication.entity.Publication;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PublicationRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String journal;
    private String conference;
    private String doi;
    private String url;
    private String authors;
    private String abstractText;
    private String team;

    @NotNull(message = "Year is required")
    @Min(value = 1900, message = "Year must be valid")
    @Max(value = 2100, message = "Year must be valid")
    private Integer year;

    @NotNull(message = "Type is required")
    private Publication.PublicationType type;
}