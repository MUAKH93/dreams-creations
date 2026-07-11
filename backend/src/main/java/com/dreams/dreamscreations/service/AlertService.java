package com.dreams.dreamscreations.service;

import com.dreams.dreamscreations.entity.Alert;
import java.util.List;

public interface AlertService {
    List<Alert> getOpenAlerts();
    Alert resolveAlert(Long alertId);
    void checkAndCreateOverdueAlerts();  // called by scheduler
}
