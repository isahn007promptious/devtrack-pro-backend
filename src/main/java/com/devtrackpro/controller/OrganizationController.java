package com.devtrackpro.controller;

import com.devtrackpro.dto.*;
import com.devtrackpro.service.OrganizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/organizations")
@Tag(name = "Organizations", description = "Endpoints for managing organizations, member invitations, and roles")
public class OrganizationController {

    private final OrganizationService organizationService;

    public OrganizationController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    @PostMapping
    @Operation(summary = "Create a new organization")
    public ResponseEntity<OrganizationResponse> createOrganization(@Valid @RequestBody OrganizationRequest request, Principal principal) {
        OrganizationResponse response = organizationService.createOrganization(request, principal.getName());
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Get all organizations the current user belongs to")
    public ResponseEntity<List<OrganizationResponse>> getUserOrganizations(Principal principal) {
        List<OrganizationResponse> response = organizationService.getUserOrganizations(principal.getName());
        return ResponseEntity.ok(response);
    }

  @PostMapping("/{id}/invitations")
@PreAuthorize("@security.hasOrgRole(#id, 'OWNER', 'ADMIN')")
@Operation(summary = "Invite a new member to the organization (Requires OWNER/ADMIN role)")
public ResponseEntity<Map<String, String>> inviteMember(@PathVariable Long id, @Valid @RequestBody InviteRequest request, Principal principal) {
    String token = organizationService.inviteMember(id, request, principal.getName());
    Map<String, String> response = new HashMap<>();
    response.put("token", token);
    response.put("message", "Invitation created successfully.");
    return ResponseEntity.ok(response);
}

@GetMapping("/{id}/invitations")
@PreAuthorize("@security.hasOrgRole(#id, 'OWNER', 'ADMIN')")
@Operation(summary = "List pending invitations for the organization")
public ResponseEntity<List<InvitationResponse>> getPendingInvitations(@PathVariable Long id) {
    List<InvitationResponse> response = organizationService.getPendingInvitations(id);
    return ResponseEntity.ok(response);
}

@PostMapping("/join")
@Operation(summary = "Accept an invitation and join an organization using just the invite token")
public ResponseEntity<Map<String, String>> joinOrganizationByToken(@Valid @RequestBody JoinRequest request, Principal principal) {
    organizationService.joinOrganization(request, principal.getName());
    Map<String, String> response = new HashMap<>();
    response.put("message", "Successfully joined the organization.");
    return ResponseEntity.ok(response);
}

    @PostMapping("/{id}/join")
    @Operation(summary = "Accept an invitation and join the organization")
    public ResponseEntity<Map<String, String>> joinOrganization(@PathVariable Long id, @Valid @RequestBody JoinRequest request, Principal principal) {
        organizationService.joinOrganization(request, principal.getName());
        Map<String, String> response = new HashMap<>();
        response.put("message", "Successfully joined the organization.");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/leave")
    @Operation(summary = "Leave the organization (OWNERs cannot leave if they are the sole owner)")
    public ResponseEntity<Map<String, String>> leaveOrganization(@PathVariable Long id, Principal principal) {
        organizationService.leaveOrganization(id, principal.getName());
        Map<String, String> response = new HashMap<>();
        response.put("message", "Successfully left the organization.");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/members")
    @PreAuthorize("@security.isOrgMember(#id)")
    @Operation(summary = "List all members of the organization (Requires organization membership)")
    public ResponseEntity<List<MemberResponse>> getMembers(@PathVariable Long id) {
        List<MemberResponse> response = organizationService.getMembers(id);
        return ResponseEntity.ok(response);
    }



}


