package com.devtrackpro.controller;

import com.devtrackpro.dto.WorkspaceRequest;
import com.devtrackpro.dto.WorkspaceResponse;
import com.devtrackpro.service.WorkspaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Workspaces", description = "Endpoints for managing workspace boundaries inside organizations")
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    public WorkspaceController(WorkspaceService workspaceService) {
        this.workspaceService = workspaceService;
    }

    @PostMapping("/organizations/{orgId}/workspaces")
    @PreAuthorize("@security.hasOrgRole(#orgId, 'OWNER', 'ADMIN', 'PROJECT_MANAGER')")
    @Operation(summary = "Create a workspace inside an organization (Requires PM+ role)")
    public ResponseEntity<WorkspaceResponse> createWorkspace(@PathVariable Long orgId,
                                                             @Valid @RequestBody WorkspaceRequest request) {
        WorkspaceResponse response = workspaceService.createWorkspace(orgId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/organizations/{orgId}/workspaces")
    @PreAuthorize("@security.isOrgMember(#orgId)")
    @Operation(summary = "List all workspaces within an organization (Requires organization membership)")
    public ResponseEntity<List<WorkspaceResponse>> getWorkspaces(@PathVariable Long orgId) {
        List<WorkspaceResponse> response = workspaceService.getWorkspaces(orgId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/workspaces/{id}")
    @PreAuthorize("@security.hasWorkspaceAccess(#id, 'OWNER', 'ADMIN', 'PROJECT_MANAGER')")
    @Operation(summary = "Update a workspace (Requires PM+ role in target workspace's organization)")
    public ResponseEntity<WorkspaceResponse> updateWorkspace(@PathVariable Long id,
                                                             @Valid @RequestBody WorkspaceRequest request) {
        WorkspaceResponse response = workspaceService.updateWorkspace(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/workspaces/{id}")
    @PreAuthorize("@security.hasWorkspaceAccess(#id, 'OWNER', 'ADMIN', 'PROJECT_MANAGER')")
    @Operation(summary = "Delete a workspace (Requires PM+ role in target workspace's organization)")
    public ResponseEntity<Void> deleteWorkspace(@PathVariable Long id) {
        workspaceService.deleteWorkspace(id);
        return ResponseEntity.noContent().build();
    }
}
