package com.devtrackpro.service;

import com.devtrackpro.dto.ActivityLogResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ActivityLogService {
    void logActivity(Long userId, String action, Long projectId, Long taskId, String details);
    Page<ActivityLogResponse> getProjectActivity(Long projectId, Pageable pageable);
}
