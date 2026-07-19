package com.devtrackpro.service;

import com.devtrackpro.dto.WorkspaceRequest;
import com.devtrackpro.dto.WorkspaceResponse;

import java.util.List;

public interface WorkspaceService {
    WorkspaceResponse createWorkspace(Long orgId, WorkspaceRequest request);
    List<WorkspaceResponse> getWorkspaces(Long orgId);
    WorkspaceResponse updateWorkspace(Long id, WorkspaceRequest request);
    void deleteWorkspace(Long id);
}
