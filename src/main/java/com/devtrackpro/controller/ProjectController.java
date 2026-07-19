package com.devtrackpro.controller;

import com.devtrackpro.dto.*;
import com.devtrackpro.service.ProjectService;
import com.devtrackpro.service.ActivityLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Projects", description = "Endpoints for managing workspace projects, milestones, and members")
public class ProjectController {

    private final ProjectService projectService;
    private final ActivityLogService activityLogService;

    public ProjectController(ProjectService projectService, ActivityLogService activityLogService) {
        this.projectService = projectService;
        this.activityLogService = activityLogService;
    }

    @PostMapping("/workspaces/{workspaceId}/projects")
    @PreAuthorize("@security.hasWorkspaceAccess(#workspaceId, 'OWNER', 'ADMIN', 'PROJECT_MANAGER')")
    @Operation(summary = "Create a new project in a workspace (Requires PM+ role)")
    public ResponseEntity<ProjectResponse> createProject(@PathVariable Long workspaceId,
                                                         @Valid @RequestBody ProjectRequest request) {
        ProjectResponse response = projectService.createProject(workspaceId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/workspaces/{workspaceId}/projects")
    @PreAuthorize("@security.isWorkspaceMember(#workspaceId)")
    @Operation(summary = "List all projects in a workspace (Requires workspace membership)")
    public ResponseEntity<List<ProjectResponse>> getWorkspaceProjects(@PathVariable Long workspaceId,
                                                                      @RequestParam(defaultValue = "false") boolean includeArchived) {
        List<ProjectResponse> response = projectService.getWorkspaceProjects(workspaceId, includeArchived);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/projects/{id}")
    @PreAuthorize("@security.isProjectMember(#id)")
    @Operation(summary = "Get project details by ID (Requires project organization membership)")
    public ResponseEntity<ProjectResponse> getProject(@PathVariable Long id) {
        ProjectResponse response = projectService.getProject(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/projects/{id}")
    @PreAuthorize("@security.hasProjectAccess(#id, 'OWNER', 'ADMIN', 'PROJECT_MANAGER')")
    @Operation(summary = "Update project details (Requires PM+ role)")
    public ResponseEntity<ProjectResponse> updateProject(@PathVariable Long id,
                                                         @Valid @RequestBody ProjectRequest request) {
        ProjectResponse response = projectService.updateProject(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/projects/{id}")
    @PreAuthorize("@security.hasProjectAccess(#id, 'OWNER', 'ADMIN', 'PROJECT_MANAGER')")
    @Operation(summary = "Delete project (Requires PM+ role)")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/projects/{id}/archive")
    @PreAuthorize("@security.hasProjectAccess(#id, 'OWNER', 'ADMIN', 'PROJECT_MANAGER')")
    @Operation(summary = "Archive project (Requires PM+ role)")
    public ResponseEntity<Void> archiveProject(@PathVariable Long id) {
        projectService.archiveProject(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/projects/{id}/restore")
    @PreAuthorize("@security.hasProjectAccess(#id, 'OWNER', 'ADMIN', 'PROJECT_MANAGER')")
    @Operation(summary = "Restore archived project (Requires PM+ role)")
    public ResponseEntity<Void> restoreProject(@PathVariable Long id) {
        projectService.restoreProject(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/projects/{id}/members")
    @PreAuthorize("@security.hasProjectAccess(#id, 'OWNER', 'ADMIN', 'PROJECT_MANAGER')")
    @Operation(summary = "Assign or remove a member from the project (Requires PM+ role)")
    public ResponseEntity<Void> updateProjectMembers(@PathVariable Long id,
                                                     @Valid @RequestBody ProjectMemberRequest request) {
        projectService.updateProjectMembers(id, request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/projects/{id}/activity")
    @PreAuthorize("@security.isProjectMember(#id)")
    @Operation(summary = "Get project activity logs (Requires project membership)")
    public ResponseEntity<Page<ActivityLogResponse>> getProjectActivity(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<ActivityLogResponse> response = activityLogService.getProjectActivity(id, pageable);
        return ResponseEntity.ok(response);
    }
}
