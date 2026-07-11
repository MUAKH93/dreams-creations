package com.dreams.dreamscreations.controller;

import com.dreams.dreamscreations.entity.Alert;
import com.dreams.dreamscreations.service.AlertService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Alert management endpoints.
 *
 * Alerts are created automatically by the @Scheduled job in AlertServiceImpl
 * every day at 8 AM. Managers view and resolve them from the dashboard.
 *
 * GET  /api/alerts          → all open alerts (dashboard widget)
 * PUT  /api/alerts/{id}/resolve → mark an alert as resolved
 * POST /api/alerts/check    → manually trigger the overdue check (useful for testing)
 */
@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    private final AlertService service;

    public AlertController(AlertService service) {
        this.service = service;
    }

    /**
     * Returns all open (unresolved) alerts.
     * The React dashboard polls this to show the red notification badge.
     */
    @GetMapping
    public ResponseEntity<List<Alert>> getOpenAlerts() {
        return ResponseEntity.ok(service.getOpenAlerts());
    }

    /**
     * Manager resolves an alert after taking action.
     * Sets status → "resolved" and records resolved_date.
     */
    @PutMapping("/{id}/resolve")
    public ResponseEntity<Alert> resolveAlert(@PathVariable Long id) {
        return ResponseEntity.ok(service.resolveAlert(id));
    }

    /**
     * Manually trigger the overdue check.
     * Useful during development/testing without waiting for the 8 AM scheduler.
     * In production you'd remove or secure this endpoint.
     */
    @PostMapping("/check")
    public ResponseEntity<String> triggerCheck() {
        service.checkAndCreateOverdueAlerts();
        return ResponseEntity.ok("Overdue check completed");
    }
}
