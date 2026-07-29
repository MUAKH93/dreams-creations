package com.dreams.dreamscreations.service.shop;

import com.dreams.dreamscreations.dto.shop.ShopModuleStatusDTO;
import com.dreams.dreamscreations.dto.shop.ShopSettingsDTO;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@ConditionalOnProperty(name = "modules.shop.enabled", havingValue = "true")
public class ShopModuleServiceImpl implements ShopModuleService {

    private final ShopSettingsService settingsService;

    public ShopModuleServiceImpl(ShopSettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @Override
    public ShopModuleStatusDTO getStatus() {
        ShopSettingsDTO settings = settingsService.getSettings();
        ShopModuleStatusDTO status = new ShopModuleStatusDTO();
        status.setCurrentPhase("S5");
        status.setCompletedPhases(List.of(
                "Scaffold", "S1 — Foundation", "S2 — Product detail & discovery",
                "S3 — Shopping cart", "S4 — Checkout & shop orders",
                "S5 — Fulfillment & operations integration"));
        status.setUpcomingPhases(List.of(
                "S6 — Payments & polish"));
        status.setStorefrontEnabled(settings.isStorefrontEnabled());
        status.setAllowGuestBrowse(settings.isAllowGuestBrowse());
        status.setMessage(
                "Phase S5 active: stock reserved on confirm, customer My Shop Orders, email notifications.");
        return status;
    }
}
