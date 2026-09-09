package com.lias.lias_backend.search.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SearchResult {
    private String type;       // MEMBER, DOCUMENT, EVENT, PUBLICATION
    private Long id;
    private String title;      // name, filename, event title, pub title
    private String subtitle;   // email, event date, pub year, etc.
}
