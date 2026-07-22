package com.devtrackpro.service.impl;

import com.devtrackpro.dto.*;
import com.devtrackpro.entity.*;
import com.devtrackpro.exception.BadRequestException;
import com.devtrackpro.exception.ResourceNotFoundException;
import com.devtrackpro.repository.*;
import com.devtrackpro.service.OrganizationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrganizationServiceImpl implements OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final OrganizationMemberRepository memberRepository;
    private final OrganizationInvitationRepository invitationRepository;
    private final UserRepository userRepository;

    public OrganizationServiceImpl(OrganizationRepository organizationRepository,
                                   OrganizationMemberRepository memberRepository,
                                   OrganizationInvitationRepository invitationRepository,
                                   UserRepository userRepository) {
        this.organizationRepository = organizationRepository;
        this.memberRepository = memberRepository;
        this.invitationRepository = invitationRepository;
        this.userRepository = userRepository;
    }

    @Override
    public OrganizationResponse createOrganization(OrganizationRequest request, String currentUserEmail) {
        if (organizationRepository.existsBySlug(request.getSlug())) {
            throw new BadRequestException("Organization slug already exists");
        }

        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUserEmail));

        Organization org = Organization.builder()
                .name(request.getName())
                .slug(request.getSlug())
                .build();
        Organization savedOrg = organizationRepository.save(org);

        OrganizationMember member = OrganizationMember.builder()
                .organization(savedOrg)
                .user(user)
                .role(OrganizationRole.OWNER)
                .build();
        memberRepository.save(member);

        return OrganizationResponse.builder()
                .id(savedOrg.getId())
                .name(savedOrg.getName())
                .slug(savedOrg.getSlug())
                .createdAt(savedOrg.getCreatedAt())
                .build();
    }

   @Override
public String inviteMember(Long orgId, InviteRequest request, String currentUserEmail) {
    Organization org = organizationRepository.findById(orgId)
            .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));

    String token = UUID.randomUUID().toString();

    invitationRepository.findByOrganizationIdAndEmail(orgId, request.getEmail())
            .ifPresent(invitationRepository::delete);

    OrganizationInvitation invitation = OrganizationInvitation.builder()
            .organization(org)
            .email(request.getEmail())
            .role(OrganizationRole.valueOf(request.getRole()))
            .token(token)
            .isAccepted(false)
            .build();
    invitationRepository.save(invitation);

    return token;
}

    @Override
    public void joinOrganization(JoinRequest request, String currentUserEmail) {
        OrganizationInvitation invitation = invitationRepository.findByToken(request.getToken())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid or expired invitation token"));

        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUserEmail));

        if (!invitation.getEmail().equalsIgnoreCase(user.getEmail())) {
            throw new BadRequestException("This invitation belongs to a different email address");
        }

        // Check if already a member
        if (memberRepository.existsByOrganizationIdAndUserId(invitation.getOrganization().getId(), user.getId())) {
            invitationRepository.delete(invitation);
            throw new BadRequestException("You are already a member of this organization");
        }

        OrganizationMember member = OrganizationMember.builder()
                .organization(invitation.getOrganization())
                .user(user)
                .role(invitation.getRole())
                .build();
        memberRepository.save(member);

        invitationRepository.delete(invitation);
    }

    @Override
    public void leaveOrganization(Long orgId, String currentUserEmail) {
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUserEmail));

        OrganizationMember member = memberRepository.findByOrganizationIdAndUserId(orgId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("You are not a member of this organization"));

        if (member.getRole() == OrganizationRole.OWNER) {
            // Count OWNERs in organization
            long ownersCount = memberRepository.findByOrganizationId(orgId).stream()
                    .filter(m -> m.getRole() == OrganizationRole.OWNER)
                    .count();
            if (ownersCount <= 1) {
                throw new BadRequestException("OWNER cannot leave their own organization unless ownership is transferred. Please assign another member as OWNER first.");
            }
        }

        memberRepository.delete(member);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MemberResponse> getMembers(Long orgId) {
        if (!organizationRepository.existsById(orgId)) {
            throw new ResourceNotFoundException("Organization not found");
        }

        return memberRepository.findByOrganizationId(orgId).stream()
                .map(m -> MemberResponse.builder()
                        .userId(m.getUser().getId())
                        .username(m.getUser().getUsername())
                        .email(m.getUser().getEmail())
                        .firstName(m.getUser().getFirstName())
                        .lastName(m.getUser().getLastName())
                        .role(m.getRole().name())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrganizationResponse> getUserOrganizations(String currentUserEmail) {
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUserEmail));

        // Find members of this user, get organizations
        return memberRepository.findAll().stream()
                .filter(m -> m.getUser().getId().equals(user.getId()))
                .map(m -> OrganizationResponse.builder()
                        .id(m.getOrganization().getId())
                        .name(m.getOrganization().getName())
                        .slug(m.getOrganization().getSlug())
                        .createdAt(m.getOrganization().getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
public List<InvitationResponse> getPendingInvitations(Long orgId) {
    return invitationRepository.findByOrganizationIdAndIsAcceptedFalse(orgId).stream()
            .map(inv -> InvitationResponse.builder()
                    .id(inv.getId())
                    .email(inv.getEmail())
                    .role(inv.getRole().name())
                    .accepted(inv.isAccepted())
                    .build())
            .toList();
}

}
