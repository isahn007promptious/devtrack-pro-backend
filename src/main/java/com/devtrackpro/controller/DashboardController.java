package com.devtrackpro.controller;

import com.devtrackpro.dto.DashboardResponse;
import com.devtrackpro.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "Dashboard", description = "Endpoints for user statistics and performance insights")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    @Operation(summary = "Get dashboard counters and weekly productivity metrics (scoped to the logged-in user)")
    public ResponseEntity<DashboardResponse> getDashboard(Principal principal) {
        DashboardResponse response = dashboardService.getDashboardData(principal.getName());
        return ResponseEntity.ok(response);
    }
}
