package com.dreams.dreamscreations.controller;

import com.dreams.dreamscreations.dto.AnalyticsDashboardDTO;
import com.dreams.dreamscreations.service.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService service;

    public AnalyticsController(AnalyticsService service) {
        this.service = service;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<AnalyticsDashboardDTO> getDashboard(
            @RequestParam(defaultValue = "6") int months) {
        return ResponseEntity.ok(service.getDashboard(months));
    }
}
