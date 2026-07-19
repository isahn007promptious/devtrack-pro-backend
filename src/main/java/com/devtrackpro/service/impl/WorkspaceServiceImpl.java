package com.devtrackpro.service.impl;

import com.devtrackpro.dto.WorkspaceRequest;
import com.devtrackpro.dto.WorkspaceResponse;
import com.devtrackpro.entity.Organization;
import com.devtrackpro.entity.Workspace;
import com.devtrackpro.exception.BadRequestException;
import com.devtrackpro.exception.ResourceNotFoundException;
import com.devtrackpro.repository.OrganizationRepository;
import com.devtrackpro.repository.WorkspaceRepository;
import com.devtrackpro.service.WorkspaceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class WorkspaceServiceImpl implements WorkspaceService {

    private final WorkspaceRepository workspaceRepository;
    private final OrganizationRepository organizationRepository;

    public WorkspaceServiceImpl(WorkspaceRepository workspaceRepository,
                                OrganizationRepository organizationRepository) {
        this.workspaceRepository = workspaceRepository;
        this.organizationRepository = organizationRepository;
    }

    @Override
    public WorkspaceResponse createWorkspace(Long orgId, WorkspaceRequest request) {
        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));

        if (workspaceRepository.findByOrganizationIdAndSlug(orgId, request.getSlug()).isPresent()) {
            throw new BadRequestException("Workspace slug already exists in this organization");
        }

        Workspace workspace = Workspace.builder()
                .organization(org)
                .name(request.getName())
                .slug(request.getSlug())
                .description(request.getDescription())
                .build();
        Workspace saved = workspaceRepository.save(workspace);

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkspaceResponse> getWorkspaces(Long orgId) {
        if (!organizationRepository.existsById(orgId)) {
            throw new ResourceNotFoundException("Organization not found");
        }
        return workspaceRepository.findByOrganizationId(orgId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public WorkspaceResponse updateWorkspace(Long id, WorkspaceRequest request) {
        Workspace ws = workspaceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found"));

        // Check unique slug within organization
        workspaceRepository.findByOrganizationIdAndSlug(ws.getOrganization().getId(), request.getSlug())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        throw new BadRequestException("Workspace slug already exists in this organization");
                    }
                });

        ws.setName(request.getName());
        ws.setSlug(request.getSlug());
        ws.setDescription(request.getDescription());

        Workspace saved = workspaceRepository.save(ws);
        return mapToResponse(saved);
    }

    @Override
    public void deleteWorkspace(Long id) {
        if (!workspaceRepository.existsById(id)) {
            throw new ResourceNotFoundException("Workspace not found");
        }
        workspaceRepository.deleteById(id);
    }

    private WorkspaceResponse mapToResponse(Workspace ws) {
        return WorkspaceResponse.builder()
                .id(ws.getId())
                .organizationId(ws.getOrganization().getId())
                .name(ws.getName())
                .slug(ws.getSlug())
                .description(ws.getDescription())
                .createdAt(ws.getCreatedAt())
                .build();
    }
}
