package com.devtrackpro.repository;
import java.util.List;
import com.devtrackpro.entity.OrganizationInvitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrganizationInvitationRepository extends JpaRepository<OrganizationInvitation, Long> {
    Optional<OrganizationInvitation> findByToken(String token);
    Optional<OrganizationInvitation> findByOrganizationIdAndEmail(Long organizationId, String email);
    List<OrganizationInvitation> findByOrganizationIdAndIsAcceptedFalse(Long organizationId);
}

