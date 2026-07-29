package com.dreams.dreamscreations.controller.shop;

import com.dreams.dreamscreations.dto.shop.ShopCatalogDesignDTO;
import com.dreams.dreamscreations.dto.shop.ShopSettingsDTO;
import com.dreams.dreamscreations.service.shop.ShopCatalogService;
import com.dreams.dreamscreations.service.shop.ShopSettingsService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shop")
@ConditionalOnProperty(name = "modules.shop.enabled", havingValue = "true")
public class ShopCatalogController {

    private final ShopCatalogService catalogService;
    private final ShopSettingsService settingsService;

    public ShopCatalogController(ShopCatalogService catalogService,
                                 ShopSettingsService settingsService) {
        this.catalogService = catalogService;
        this.settingsService = settingsService;
    }

    /** Public storefront header info — no auth required. */
    @GetMapping("/settings/public")
    public ResponseEntity<ShopSettingsDTO> publicSettings() {
        return ResponseEntity.ok(settingsService.getSettings());
    }

    /** Public catalog — active designs with variants and stock. */
    @GetMapping("/catalog")
    public ResponseEntity<List<ShopCatalogDesignDTO>> catalog(
            @RequestParam(required = false) Boolean featured) {
        return ResponseEntity.ok(catalogService.getCatalog(featured));
    }

    @GetMapping("/catalog/{designId}")
    public ResponseEntity<ShopCatalogDesignDTO> designDetail(@PathVariable Long designId) {
        return ResponseEntity.ok(catalogService.getDesign(designId));
    }
}
