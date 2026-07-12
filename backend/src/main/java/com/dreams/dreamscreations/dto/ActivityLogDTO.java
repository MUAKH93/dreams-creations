package com.dreams.dreamscreations.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ActivityLogDTO {
    private Long activityId;
    private String actionType;
    private String entityType;
    private Long entityId;
    private String summary;
    private String performedByUsername;
    private LocalDateTime createdAt;
}
