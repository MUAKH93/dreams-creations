package com.dreams.dreamscreations.service.shop;

import com.dreams.dreamscreations.dto.shop.ShopSettingsDTO;

public interface ShopSettingsService {

    ShopSettingsDTO getSettings();

    ShopSettingsDTO updateSettings(ShopSettingsDTO request);
}
