package com.devtrackpro.service;

import com.devtrackpro.dto.SearchResponse;

public interface SearchService {
    SearchResponse search(String query, String currentUserEmail);
}
