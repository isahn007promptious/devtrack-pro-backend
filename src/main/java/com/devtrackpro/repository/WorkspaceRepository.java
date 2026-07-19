package com.devtrackpro.repository;

import com.devtrackpro.entity.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkspaceRepository extends JpaRepository<Workspace, Long> {
    List<Workspace> findByOrganizationId(Long organizationId);
    Optional<Workspace> findByOrganizationIdAndSlug(Long organizationId, String slug);
}
