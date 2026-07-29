package com.dreams.dreamscreations.service.shop;

import com.dreams.dreamscreations.dto.shop.ShopSettingsDTO;
import com.dreams.dreamscreations.entity.shop.ShopSettings;
import com.dreams.dreamscreations.repository.shop.ShopSettingsRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@ConditionalOnProperty(name = "modules.shop.enabled", havingValue = "true")
public class ShopSettingsServiceImpl implements ShopSettingsService {

    private final ShopSettingsRepository settingsRepo;

    public ShopSettingsServiceImpl(ShopSettingsRepository settingsRepo) {
        this.settingsRepo = settingsRepo;
    }

    @Override
    @Transactional(readOnly = true)
    public ShopSettingsDTO getSettings() {
        return ShopSettingsDTO.fromEntity(loadSettings());
    }

    @Override
    @Transactional
    public ShopSettingsDTO updateSettings(ShopSettingsDTO request) {
        ShopSettings settings = loadSettings();
        if (request.getStoreName() != null && !request.getStoreName().isBlank()) {
            settings.setStoreName(request.getStoreName().trim());
        }
        if (request.getTagline() != null) {
            settings.setTagline(request.getTagline().trim());
        }
        settings.setStorefrontEnabled(request.isStorefrontEnabled());
        settings.setAllowGuestBrowse(request.isAllowGuestBrowse());
        return ShopSettingsDTO.fromEntity(settingsRepo.save(settings));
    }

    private ShopSettings loadSettings() {
        return settingsRepo.findById(1L).orElseGet(() -> {
            ShopSettings defaults = ShopSettings.builder()
                    .settingsId(1L)
                    .storeName("Dreams Creations Shop")
                    .tagline("Premium suits — order online")
                    .storefrontEnabled(true)
                    .allowGuestBrowse(true)
                    .build();
            return settingsRepo.save(defaults);
        });
    }
}
