package com.devtrackpro.service;

import com.devtrackpro.dto.DashboardResponse;

public interface DashboardService {
    DashboardResponse getDashboardData(String currentUserEmail);
}
