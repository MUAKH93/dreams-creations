package com.dreams.dreamscreations.service;

import com.dreams.dreamscreations.dto.AnalyticsDashboardDTO;

public interface AnalyticsService {
    AnalyticsDashboardDTO getDashboard(int months);
}
