package com.devtrackpro.service.impl;

import com.devtrackpro.dto.*;
import com.devtrackpro.entity.*;
import com.devtrackpro.exception.BadRequestException;
import com.devtrackpro.exception.ResourceNotFoundException;
import com.devtrackpro.repository.OrganizationMemberRepository;
import com.devtrackpro.repository.ProjectRepository;
import com.devtrackpro.repository.TaskRepository;
import com.devtrackpro.repository.UserRepository;
import com.devtrackpro.repository.WorkspaceRepository;
import com.devtrackpro.service.ActivityLogService;
import com.devtrackpro.service.ProjectService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final WorkspaceRepository workspaceRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final OrganizationMemberRepository memberRepository;
    private final ActivityLogService activityLogService;

    public ProjectServiceImpl(ProjectRepository projectRepository,
                              WorkspaceRepository workspaceRepository,
                              UserRepository userRepository,
                              TaskRepository taskRepository,
                              OrganizationMemberRepository memberRepository,
                              ActivityLogService activityLogService) {
        this.projectRepository = projectRepository;
        this.workspaceRepository = workspaceRepository;
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.memberRepository = memberRepository;
        this.activityLogService = activityLogService;
    }

    @Override
    public ProjectResponse createProject(Long workspaceId, ProjectRequest request) {
        Workspace ws = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found"));

        if (projectRepository.findByWorkspaceIdAndKeyPrefix(workspaceId, request.getKeyPrefix()).isPresent()) {
            throw new BadRequestException("Project key prefix already exists in this workspace");
        }

        User currentUser = getAuthenticatedUser();

        Project project = Project.builder()
                .workspace(ws)
                .name(request.getName())
                .keyPrefix(request.getKeyPrefix().toUpperCase())
                .description(request.getDescription())
                .deadline(request.getDeadline())
                .priority(ProjectPriority.valueOf(request.getPriority()))
                .isArchived(false)
                .build();

        // Creator automatically is a member of the project
        project.getMembers().add(currentUser);

        Project saved = projectRepository.save(project);

        // Log Activity
        activityLogService.logActivity(
                currentUser.getId(),
                "CREATE_PROJECT",
                saved.getId(),
                null,
                currentUser.getFirstName() + " " + currentUser.getLastName() + " created Project " + saved.getName()
        );

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectResponse getProject(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
        return mapToResponse(project);
    }

    @Override
    public ProjectResponse updateProject(Long id, ProjectRequest request) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        // Check key prefix unique constraints
        projectRepository.findByWorkspaceIdAndKeyPrefix(project.getWorkspace().getId(), request.getKeyPrefix())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        throw new BadRequestException("Project key prefix already exists in this workspace");
                    }
                });

        User currentUser = getAuthenticatedUser();

        project.setName(request.getName());
        project.setKeyPrefix(request.getKeyPrefix().toUpperCase());
        project.setDescription(request.getDescription());
        project.setDeadline(request.getDeadline());
        project.setPriority(ProjectPriority.valueOf(request.getPriority()));

        Project saved = projectRepository.save(project);

        // Log Activity
        activityLogService.logActivity(
                currentUser.getId(),
                "UPDATE_PROJECT",
                saved.getId(),
                null,
                currentUser.getFirstName() + " " + currentUser.getLastName() + " updated Project details"
        );

        return mapToResponse(saved);
    }

    @Override
    public void deleteProject(Long id) {
        if (!projectRepository.existsById(id)) {
            throw new ResourceNotFoundException("Project not found");
        }
        projectRepository.deleteById(id);
    }

    @Override
    public void archiveProject(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
        project.setArchived(true);
        projectRepository.save(project);

        User currentUser = getAuthenticatedUser();
        activityLogService.logActivity(
                currentUser.getId(),
                "ARCHIVE_PROJECT",
                id,
                null,
                currentUser.getFirstName() + " " + currentUser.getLastName() + " archived Project " + project.getName()
        );
    }

    @Override
    public void restoreProject(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
        project.setArchived(false);
        projectRepository.save(project);

        User currentUser = getAuthenticatedUser();
        activityLogService.logActivity(
                currentUser.getId(),
                "RESTORE_PROJECT",
                id,
                null,
                currentUser.getFirstName() + " " + currentUser.getLastName() + " restored Project " + project.getName()
        );
    }

    @Override
    public void updateProjectMembers(Long id, ProjectMemberRequest request) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        User targetUser = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Validate user belongs to organization
        Long orgId = project.getWorkspace().getOrganization().getId();
        if (!memberRepository.existsByOrganizationIdAndUserId(orgId, targetUser.getId())) {
            throw new BadRequestException("User must belong to the organization before they can be added to the project");
        }

        User currentUser = getAuthenticatedUser();

        if ("ADD".equalsIgnoreCase(request.getAction())) {
            project.getMembers().add(targetUser);
            activityLogService.logActivity(
                    currentUser.getId(),
                    "ADD_MEMBER",
                    id,
                    null,
                    currentUser.getFirstName() + " " + currentUser.getLastName() + " added " + targetUser.getFirstName() + " " + targetUser.getLastName() + " to the project"
            );
        } else {
            project.getMembers().remove(targetUser);
            activityLogService.logActivity(
                    currentUser.getId(),
                    "REMOVE_MEMBER",
                    id,
                    null,
                    currentUser.getFirstName() + " " + currentUser.getLastName() + " removed " + targetUser.getFirstName() + " " + targetUser.getLastName() + " from the project"
            );
        }

        projectRepository.save(project);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectResponse> getWorkspaceProjects(Long workspaceId, boolean includeArchived) {
        if (!workspaceRepository.existsById(workspaceId)) {
            throw new ResourceNotFoundException("Workspace not found");
        }

        List<Project> projects;
        if (includeArchived) {
            projects = projectRepository.findByWorkspaceId(workspaceId);
        } else {
            projects = projectRepository.findByWorkspaceIdAndIsArchived(workspaceId, false);
        }

        return projects.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private ProjectResponse mapToResponse(Project project) {
        Double progress = calculateProjectProgress(project.getId());
        List<UserSummaryResponse> members = project.getMembers().stream()
                .map(m -> UserSummaryResponse.builder()
                        .id(m.getId())
                        .username(m.getUsername())
                        .email(m.getEmail())
                        .firstName(m.getFirstName())
                        .lastName(m.getLastName())
                        .avatarUrl(m.getAvatarUrl())
                        .build())
                .collect(Collectors.toList());

        return ProjectResponse.builder()
                .id(project.getId())
                .workspaceId(project.getWorkspace().getId())
                .name(project.getName())
                .keyPrefix(project.getKeyPrefix())
                .description(project.getDescription())
                .deadline(project.getDeadline())
                .priority(project.getPriority().name())
                .progress(progress)
                .isArchived(project.isArchived())
                .members(members)
                .createdAt(project.getCreatedAt())
                .build();
    }

    private Double calculateProjectProgress(Long projectId) {
        long totalTasks = taskRepository.countByProjectId(projectId);
        if (totalTasks == 0) {
            return 0.0;
        }
        long doneTasks = taskRepository.countByProjectIdAndStatus(projectId, TaskStatus.DONE);
        return ((double) doneTasks / totalTasks) * 100.0;
    }

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new org.springframework.security.authentication.BadCredentialsException("User not authenticated");
        }
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + auth.getName()));
    }
}
