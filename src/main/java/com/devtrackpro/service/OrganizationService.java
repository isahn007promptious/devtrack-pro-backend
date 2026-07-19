package com.devtrackpro.service;

import com.devtrackpro.dto.*;

import java.util.List;

public interface OrganizationService {
    OrganizationResponse createOrganization(OrganizationRequest request, String currentUserEmail);
    void inviteMember(Long orgId, InviteRequest request, String currentUserEmail);
    void joinOrganization(JoinRequest request, String currentUserEmail);
    void leaveOrganization(Long orgId, String currentUserEmail);
    List<MemberResponse> getMembers(Long orgId);
    List<OrganizationResponse> getUserOrganizations(String currentUserEmail);
}
