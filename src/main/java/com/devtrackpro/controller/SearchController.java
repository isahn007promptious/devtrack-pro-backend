package com.devtrackpro.controller;

import com.devtrackpro.dto.SearchResponse;
import com.devtrackpro.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/search")
@Tag(name = "Search", description = "Endpoints for global searching across projects, tasks, users, and labels")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping
    @Operation(summary = "Perform a global keyword search across accessible projects, tasks, users, and labels")
    public ResponseEntity<SearchResponse> search(@RequestParam String q, Principal principal) {
        SearchResponse response = searchService.search(q, principal.getName());
        return ResponseEntity.ok(response);
    }
}
