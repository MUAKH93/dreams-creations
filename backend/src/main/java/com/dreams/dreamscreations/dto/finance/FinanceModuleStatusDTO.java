package com.dreams.dreamscreations.dto.finance;

import java.util.List;

public class FinanceModuleStatusDTO {

    private String version = "2.0-scaffold";
    private String branch = "feature/finance-v2";
    private String currentPhase = "F0";
    private List<String> completedPhases;
    private List<String> upcomingPhases;
    private boolean autoPostAr;
    private boolean autoPostInventory;
    private String message;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getCurrentPhase() {
        return currentPhase;
    }

    public void setCurrentPhase(String currentPhase) {
        this.currentPhase = currentPhase;
    }

    public List<String> getCompletedPhases() {
        return completedPhases;
    }

    public void setCompletedPhases(List<String> completedPhases) {
        this.completedPhases = completedPhases;
    }

    public List<String> getUpcomingPhases() {
        return upcomingPhases;
    }

    public void setUpcomingPhases(List<String> upcomingPhases) {
        this.upcomingPhases = upcomingPhases;
    }

    public boolean isAutoPostAr() {
        return autoPostAr;
    }

    public void setAutoPostAr(boolean autoPostAr) {
        this.autoPostAr = autoPostAr;
    }

    public boolean isAutoPostInventory() {
        return autoPostInventory;
    }

    public void setAutoPostInventory(boolean autoPostInventory) {
        this.autoPostInventory = autoPostInventory;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
