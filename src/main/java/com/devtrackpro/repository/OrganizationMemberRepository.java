package com.devtrackpro.repository;

import com.devtrackpro.entity.OrganizationMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrganizationMemberRepository extends JpaRepository<OrganizationMember, Long> {
    Optional<OrganizationMember> findByOrganizationIdAndUserId(Long organizationId, Long userId);
    Optional<OrganizationMember> findByOrganizationIdAndUserEmail(Long organizationId, String email);
    List<OrganizationMember> findByOrganizationId(Long organizationId);
    boolean existsByOrganizationIdAndUserId(Long organizationId, Long userId);
}
