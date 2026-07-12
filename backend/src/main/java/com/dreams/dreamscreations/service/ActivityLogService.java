package com.dreams.dreamscreations.service;

import com.dreams.dreamscreations.dto.ActivityLogDTO;
import com.dreams.dreamscreations.entity.User;

import java.util.List;

public interface ActivityLogService {
    void log(String actionType, String entityType, Long entityId, String summary);
    void log(User user, String actionType, String entityType, Long entityId, String summary);
    List<ActivityLogDTO> getRecent();
}
