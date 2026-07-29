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
        status.setCurrentPhase("S4");
        status.setCompletedPhases(List.of(
                "Scaffold", "S1 — Foundation", "S2 — Product detail & discovery",
                "S3 — Shopping cart", "S4 — Checkout & shop orders"));
        status.setUpcomingPhases(List.of(
                "S5 — Fulfillment & operations integration",
                "S6 — Payments & polish"));
        status.setStorefrontEnabled(settings.isStorefrontEnabled());
        status.setAllowGuestBrowse(settings.isAllowGuestBrowse());
        status.setMessage(
                "Phase S4 active: customers can checkout at /store/checkout; review orders in Shop Portal.");
        return status;
    }
}
