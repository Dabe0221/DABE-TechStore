package com.ecommerce.demo_ecommerce.service;

import com.ecommerce.demo_ecommerce.entity.ActivityLog;
import com.ecommerce.demo_ecommerce.repository.ActivityLogRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ActivityLogService {

    private final ActivityLogRepository activityLogRepository;

    public ActivityLogService(
            ActivityLogRepository activityLogRepository
    ) {
        this.activityLogRepository = activityLogRepository;
    }

    public void log(
            String action,
            String description,
            Authentication authentication
    ) {

        String performedBy = "System";

        if (authentication != null
                && authentication.isAuthenticated()) {
            performedBy = authentication.getName();
        }

        ActivityLog activityLog = new ActivityLog(
                action,
                description,
                performedBy
        );

        activityLogRepository.save(activityLog);
    }

    public List<ActivityLog> getRecentActivities() {
        return activityLogRepository
                .findTop20ByOrderByCreatedAtDesc();
    }
}