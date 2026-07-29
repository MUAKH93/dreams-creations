package com.dreams.dreamscreations.dto.shop;

public class ShopSettingsDTO {

    private String storeName;
    private String tagline;
    private boolean storefrontEnabled;
    private boolean allowGuestBrowse;

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public String getTagline() {
        return tagline;
    }

    public void setTagline(String tagline) {
        this.tagline = tagline;
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

    public static ShopSettingsDTO fromEntity(com.dreams.dreamscreations.entity.shop.ShopSettings settings) {
        ShopSettingsDTO dto = new ShopSettingsDTO();
        dto.setStoreName(settings.getStoreName());
        dto.setTagline(settings.getTagline());
        dto.setStorefrontEnabled(Boolean.TRUE.equals(settings.getStorefrontEnabled()));
        dto.setAllowGuestBrowse(Boolean.TRUE.equals(settings.getAllowGuestBrowse()));
        return dto;
    }
}
