package com.dreams.dreamscreations.service.impl;

import com.dreams.dreamscreations.dto.ActivityLogDTO;
import com.dreams.dreamscreations.entity.ActivityLog;
import com.dreams.dreamscreations.entity.User;
import com.dreams.dreamscreations.repository.ActivityLogRepository;
import com.dreams.dreamscreations.security.CurrentUserService;
import com.dreams.dreamscreations.service.ActivityLogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ActivityLogServiceImpl implements ActivityLogService {

    private final ActivityLogRepository activityLogRepo;
    private final CurrentUserService currentUserService;

    public ActivityLogServiceImpl(ActivityLogRepository activityLogRepo,
                                  CurrentUserService currentUserService) {
        this.activityLogRepo = activityLogRepo;
        this.currentUserService = currentUserService;
    }

    @Override
    @Transactional
    public void log(String actionType, String entityType, Long entityId, String summary) {
        User user = null;
        try {
            user = currentUserService.getCurrentUser();
        } catch (RuntimeException ignored) {
            // Scheduler or unauthenticated context — log without user.
        }
        log(user, actionType, entityType, entityId, summary);
    }

    @Override
    @Transactional
    public void log(User user, String actionType, String entityType, Long entityId, String summary) {
        activityLogRepo.save(ActivityLog.builder()
                .actionType(actionType)
                .entityType(entityType)
                .entityId(entityId)
                .summary(summary)
                .performedBy(user)
                .build());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActivityLogDTO> getRecent() {
        return activityLogRepo.findAllWithUser().stream()
                .map(this::toDto)
                .toList();
    }

    private ActivityLogDTO toDto(ActivityLog log) {
        return ActivityLogDTO.builder()
                .activityId(log.getActivityId())
                .actionType(log.getActionType())
                .entityType(log.getEntityType())
                .entityId(log.getEntityId())
                .summary(log.getSummary())
                .performedByUsername(log.getPerformedBy() != null ? log.getPerformedBy().getUsername() : "system")
                .createdAt(log.getCreatedAt())
                .build();
    }
}
