package com.devtrackpro.service;

import com.devtrackpro.dto.*;

import java.util.List;

public interface ProjectService {
    ProjectResponse createProject(Long workspaceId, ProjectRequest request);
    ProjectResponse getProject(Long id);
    ProjectResponse updateProject(Long id, ProjectRequest request);
    void deleteProject(Long id);
    void archiveProject(Long id);
    void restoreProject(Long id);
    void updateProjectMembers(Long id, ProjectMemberRequest request);
    List<ProjectResponse> getWorkspaceProjects(Long workspaceId, boolean includeArchived);
}
