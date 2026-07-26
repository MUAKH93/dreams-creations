package com.dreams.dreamscreations.service;

import com.dreams.dreamscreations.dto.DashboardChartsDTO;
import com.dreams.dreamscreations.dto.DashboardSummaryDTO;

public interface DashboardService {
    DashboardSummaryDTO getSummary();
    DashboardChartsDTO getCharts();
}
