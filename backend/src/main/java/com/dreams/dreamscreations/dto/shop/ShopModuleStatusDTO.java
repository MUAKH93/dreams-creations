package com.dreams.dreamscreations.dto.shop;

import java.util.List;

public class ShopModuleStatusDTO {

    private String version = "1.0-scaffold";
    private String branch = "feature/shop-v1";
    private String currentPhase = "S1";
    private List<String> completedPhases;
    private List<String> upcomingPhases;
    private boolean storefrontEnabled;
    private boolean allowGuestBrowse;
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

    public boolean isStorefrontEnabled() {
        return storefrontEnabled;
    }

    public void setStorefrontEnabled(boolean storefrontEnabled) {
        this.storefrontEnabled = storefrontEnabled;
    }

    public boolean isAllowGuestBrowse() {
        return allowGuestBrowse;
    }

    public void setAllowGuestBrowse(boolean allowGuestBrowse) {
        this.allowGuestBrowse = allowGuestBrowse;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
