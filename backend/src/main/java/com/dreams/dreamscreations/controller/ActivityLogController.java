package com.dreams.dreamscreations.controller;

import com.dreams.dreamscreations.dto.ActivityLogDTO;
import com.dreams.dreamscreations.service.ActivityLogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/activity-log")
public class ActivityLogController {

    private final ActivityLogService activityLogService;

    public ActivityLogController(ActivityLogService activityLogService) {
        this.activityLogService = activityLogService;
    }

    @GetMapping
    public ResponseEntity<List<ActivityLogDTO>> getRecent() {
        return ResponseEntity.ok(activityLogService.getRecent());
    }
}
