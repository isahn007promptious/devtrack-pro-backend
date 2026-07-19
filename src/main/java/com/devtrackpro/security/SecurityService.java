package com.devtrackpro.security;

import com.devtrackpro.entity.OrganizationMember;
import com.devtrackpro.entity.Project;
import com.devtrackpro.entity.Workspace;
import com.devtrackpro.entity.Sprint;
import com.devtrackpro.entity.Task;
import com.devtrackpro.repository.OrganizationMemberRepository;
import com.devtrackpro.repository.ProjectRepository;
import com.devtrackpro.repository.WorkspaceRepository;
import com.devtrackpro.repository.SprintRepository;
import com.devtrackpro.repository.TaskRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("security")
public class SecurityService {

    private final OrganizationMemberRepository memberRepository;
    private final WorkspaceRepository workspaceRepository;
    private final ProjectRepository projectRepository;
    private final SprintRepository sprintRepository;
    private final TaskRepository taskRepository;

    public SecurityService(OrganizationMemberRepository memberRepository,
                           WorkspaceRepository workspaceRepository,
                           ProjectRepository projectRepository,
                           SprintRepository sprintRepository,
                           TaskRepository taskRepository) {
        this.memberRepository = memberRepository;
        this.workspaceRepository = workspaceRepository;
        this.projectRepository = projectRepository;
        this.sprintRepository = sprintRepository;
        this.taskRepository = taskRepository;
    }

    public boolean hasOrgRole(Long orgId, String... roles) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }
        String email = auth.getName();
        Optional<OrganizationMember> memberOpt = memberRepository.findByOrganizationIdAndUserEmail(orgId, email);
        if (memberOpt.isEmpty()) {
            return false;
        }

        String userRole = memberOpt.get().getRole().name();
        for (String role : roles) {
            if (userRole.equals(role)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasWorkspaceAccess(Long workspaceId, String... roles) {
        Optional<Workspace> workspaceOpt = workspaceRepository.findById(workspaceId);
        if (workspaceOpt.isEmpty()) {
            return false;
        }
        return hasOrgRole(workspaceOpt.get().getOrganization().getId(), roles);
    }

    public boolean hasProjectAccess(Long projectId, String... roles) {
        Optional<Project> projectOpt = projectRepository.findById(projectId);
        if (projectOpt.isEmpty()) {
            return false;
        }
        return hasOrgRole(projectOpt.get().getWorkspace().getOrganization().getId(), roles);
    }

    public boolean hasSprintAccess(Long sprintId, String... roles) {
        Optional<Sprint> sprintOpt = sprintRepository.findById(sprintId);
        if (sprintOpt.isEmpty()) {
            return false;
        }
        return hasOrgRole(sprintOpt.get().getProject().getWorkspace().getOrganization().getId(), roles);
    }

    public boolean hasTaskAccess(Long taskId, String... roles) {
        Optional<Task> taskOpt = taskRepository.findById(taskId);
        if (taskOpt.isEmpty()) {
            return false;
        }
        return hasOrgRole(taskOpt.get().getProject().getWorkspace().getOrganization().getId(), roles);
    }

    public boolean isOrgMember(Long orgId) {
        return hasOrgRole(orgId, "OWNER", "ADMIN", "PROJECT_MANAGER", "DEVELOPER", "VIEWER");
    }

    public boolean isWorkspaceMember(Long workspaceId) {
        return hasWorkspaceAccess(workspaceId, "OWNER", "ADMIN", "PROJECT_MANAGER", "DEVELOPER", "VIEWER");
    }

    public boolean isProjectMember(Long projectId) {
        return hasProjectAccess(projectId, "OWNER", "ADMIN", "PROJECT_MANAGER", "DEVELOPER", "VIEWER");
    }

    public boolean isSprintMember(Long sprintId) {
        return hasSprintAccess(sprintId, "OWNER", "ADMIN", "PROJECT_MANAGER", "DEVELOPER", "VIEWER");
    }

    public boolean isTaskMember(Long taskId) {
        return hasTaskAccess(taskId, "OWNER", "ADMIN", "PROJECT_MANAGER", "DEVELOPER", "VIEWER");
    }
}
