package com.devtrackpro.service;

import com.devtrackpro.dto.SprintRequest;
import com.devtrackpro.dto.SprintResponse;

import java.util.List;

public interface SprintService {
    SprintResponse createSprint(Long projectId, SprintRequest request);
    SprintResponse getSprint(Long id);
    SprintResponse updateSprint(Long id, SprintRequest request);
    void deleteSprint(Long id);
    List<SprintResponse> getProjectSprints(Long projectId);
}
