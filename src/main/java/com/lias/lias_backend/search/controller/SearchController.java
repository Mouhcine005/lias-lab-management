package com.lias.lias_backend.search.controller;

import com.lias.lias_backend.search.dto.SearchResult;
import com.lias.lias_backend.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @GetMapping
    public ResponseEntity<List<SearchResult>> search(@RequestParam(name = "q", required = false) String q) {
        if (q == null || q.isBlank()) return ResponseEntity.ok(List.of());
        return ResponseEntity.ok(searchService.search(q));
    }
}
