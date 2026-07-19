package com.devtrackpro.controller;

import com.devtrackpro.dto.SprintRequest;
import com.devtrackpro.dto.SprintResponse;
import com.devtrackpro.service.SprintService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Sprints", description = "Endpoints for managing timeboxed task iterations inside projects")
public class SprintController {

    private final SprintService sprintService;

    public SprintController(SprintService sprintService) {
        this.sprintService = sprintService;
    }

    @PostMapping("/projects/{projectId}/sprints")
    @PreAuthorize("@security.hasProjectAccess(#projectId, 'OWNER', 'ADMIN', 'PROJECT_MANAGER')")
    @Operation(summary = "Create a new sprint inside a project (Requires PM+ role)")
    public ResponseEntity<SprintResponse> createSprint(@PathVariable Long projectId,
                                                       @Valid @RequestBody SprintRequest request) {
        SprintResponse response = sprintService.createSprint(projectId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/projects/{projectId}/sprints")
    @PreAuthorize("@security.isProjectMember(#projectId)")
    @Operation(summary = "List all sprints of a project (Requires project membership)")
    public ResponseEntity<List<SprintResponse>> getProjectSprints(@PathVariable Long projectId) {
        List<SprintResponse> response = sprintService.getProjectSprints(projectId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/sprints/{id}")
    @PreAuthorize("@security.hasSprintAccess(#id, 'OWNER', 'ADMIN', 'PROJECT_MANAGER')")
    @Operation(summary = "Update sprint details (Requires PM+ role in target sprint's organization)")
    public ResponseEntity<SprintResponse> updateSprint(@PathVariable Long id,
                                                       @Valid @RequestBody SprintRequest request) {
        SprintResponse response = sprintService.updateSprint(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/sprints/{id}")
    @PreAuthorize("@security.hasSprintAccess(#id, 'OWNER', 'ADMIN', 'PROJECT_MANAGER')")
    @Operation(summary = "Delete a sprint (Requires PM+ role in target sprint's organization)")
    public ResponseEntity<Void> deleteSprint(@PathVariable Long id) {
        sprintService.deleteSprint(id);
        return ResponseEntity.noContent().build();
    }
}
