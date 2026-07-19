package com.devtrackpro.repository;

import com.devtrackpro.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByWorkspaceId(Long workspaceId);
    List<Project> findByWorkspaceIdAndIsArchived(Long workspaceId, boolean isArchived);
    Optional<Project> findByWorkspaceIdAndKeyPrefix(Long workspaceId, String keyPrefix);

    @org.springframework.data.jpa.repository.Query("SELECT p FROM Project p WHERE p.workspace.organization.id IN " +
           "(SELECT om.organization.id FROM OrganizationMember om WHERE om.user.id = :userId)")
    List<Project> findAllAccessibleProjects(@org.springframework.data.repository.query.Param("userId") Long userId);
}
