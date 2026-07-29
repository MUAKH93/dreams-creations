package com.dreams.dreamscreations.service.shop;

import com.dreams.dreamscreations.dto.shop.ShopCatalogDesignDTO;
import com.dreams.dreamscreations.dto.shop.ShopCatalogVariantDTO;
import com.dreams.dreamscreations.entity.Design;
import com.dreams.dreamscreations.entity.DesignImage;
import com.dreams.dreamscreations.entity.Product;
import com.dreams.dreamscreations.entity.Suit;
import com.dreams.dreamscreations.repository.DesignRepository;
import com.dreams.dreamscreations.repository.ProductRepository;
import com.dreams.dreamscreations.service.InventoryService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@ConditionalOnProperty(name = "modules.shop.enabled", havingValue = "true")
public class ShopCatalogServiceImpl implements ShopCatalogService {

    private final ProductRepository productRepo;
    private final DesignRepository designRepo;
    private final InventoryService inventoryService;
    private final ShopSettingsService settingsService;

    public ShopCatalogServiceImpl(ProductRepository productRepo,
                                  DesignRepository designRepo,
                                  InventoryService inventoryService,
                                  ShopSettingsService settingsService) {
        this.productRepo = productRepo;
        this.designRepo = designRepo;
        this.inventoryService = inventoryService;
        this.settingsService = settingsService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShopCatalogDesignDTO> getCatalog(Boolean featuredOnly, String category, String query) {
        ensureStorefrontOpen();
        Map<Long, Design> designsWithImages = designRepo.findAllWithImages().stream()
                .collect(Collectors.toMap(Design::getDesignId, d -> d, (a, b) -> a));
        Map<Long, ShopCatalogDesignDTO> byDesign = new LinkedHashMap<>();

        for (Product product : productRepo.findAllActiveWithSuitDetails()) {
            Suit suit = product.getSuit();
            if (suit == null || suit.getDesign() == null) {
                continue;
            }
            Design design = suit.getDesign();
            if (!"active".equalsIgnoreCase(design.getStatus())) {
                continue;
            }
            if (Boolean.TRUE.equals(featuredOnly) && !Boolean.TRUE.equals(design.getIsFeatured())) {
                continue;
            }

            Design designForDisplay = designsWithImages.getOrDefault(design.getDesignId(), design);
            ShopCatalogDesignDTO row = byDesign.computeIfAbsent(
                    design.getDesignId(), id -> toDesignSummary(designForDisplay));
            ShopCatalogVariantDTO variant = toVariant(product, suit);
            row.getVariants().add(variant);
            row.setTotalStock(row.getTotalStock() + variant.getStockQty());
        }

        String categoryFilter = category != null ? category.trim() : null;
        String queryFilter = query != null ? query.trim().toLowerCase() : null;

        return byDesign.values().stream()
                .filter(row -> categoryFilter == null || categoryFilter.isEmpty()
                        || categoryFilter.equalsIgnoreCase(row.getCategoryName()))
                .filter(row -> {
                    if (queryFilter == null || queryFilter.isEmpty()) {
                        return true;
                    }
                    String name = row.getName() != null ? row.getName().toLowerCase() : "";
                    String code = row.getDesignCode() != null ? row.getDesignCode().toLowerCase() : "";
                    return name.contains(queryFilter) || code.contains(queryFilter);
                })
                .sorted(Comparator
                        .comparing(ShopCatalogDesignDTO::isFeatured).reversed()
                        .thenComparing(ShopCatalogDesignDTO::getDesignCode))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ShopCatalogDesignDTO getDesign(Long designId) {
        ensureStorefrontOpen();
        Design design = designRepo.findByIdWithImages(designId)
                .orElseThrow(() -> new RuntimeException("Design not found: " + designId));
        if (!"active".equalsIgnoreCase(design.getStatus())) {
            throw new RuntimeException("Design is not available in the shop");
        }

        ShopCatalogDesignDTO dto = toDesignSummary(design);
        dto.setDescription(design.getDescription());

        for (Product product : productRepo.findAllActiveWithSuitDetails()) {
            Suit suit = product.getSuit();
            if (suit == null || suit.getDesign() == null) {
                continue;
            }
            if (!designId.equals(suit.getDesign().getDesignId())) {
                continue;
            }
            ShopCatalogVariantDTO variant = toVariant(product, suit);
            dto.getVariants().add(variant);
            dto.setTotalStock(dto.getTotalStock() + variant.getStockQty());
        }

        dto.getVariants().sort(Comparator
                .comparing(ShopCatalogVariantDTO::getSizeValue, Comparator.nullsLast(String::compareTo))
                .thenComparing(ShopCatalogVariantDTO::getColor, Comparator.nullsLast(String::compareTo)));

        return dto;
    }

    private void ensureStorefrontOpen() {
        if (!settingsService.getSettings().isStorefrontEnabled()) {
            throw new RuntimeException("Online storefront is temporarily closed");
        }
    }

    private ShopCatalogDesignDTO toDesignSummary(Design design) {
        ShopCatalogDesignDTO dto = new ShopCatalogDesignDTO();
        dto.setDesignId(design.getDesignId());
        dto.setDesignCode(design.getDesignCode());
        dto.setName(design.getName());
        dto.setDescription(design.getDescription());
        dto.setBasePrice(design.getBasePrice() != null ? design.getBasePrice() : BigDecimal.ZERO);
        dto.setFeatured(Boolean.TRUE.equals(design.getIsFeatured()));
        if (design.getCategory() != null) {
            dto.setCategoryName(design.getCategory().getCategoryName());
        }
        dto.setPrimaryImageUrl(resolvePrimaryImage(design));
        dto.setVariants(new ArrayList<>());
        return dto;
    }

    private ShopCatalogVariantDTO toVariant(Product product, Suit suit) {
        int stockQty = inventoryService.getQuantity(suit);
        ShopCatalogVariantDTO variant = new ShopCatalogVariantDTO();
        variant.setProductId(product.getProductId());
        variant.setSuitId(suit.getSuitId());
        if (suit.getSize() != null) {
            variant.setSizeValue(suit.getSize().getSizeValue());
        }
        variant.setColor(suit.getColor());
        variant.setSellingPrice(product.getSellingPrice() != null
                ? product.getSellingPrice()
                : (suit.getDesign().getBasePrice() != null ? suit.getDesign().getBasePrice() : BigDecimal.ZERO));
        variant.setStockQty(stockQty);
        variant.setInStock(stockQty > 0);
        return variant;
    }

    private String resolvePrimaryImage(Design design) {
        if (design.getImages() == null || design.getImages().isEmpty()) {
            return null;
        }
        DesignImage primary = design.getImages().stream()
                .filter(img -> Boolean.TRUE.equals(img.getIsPrimary()))
                .findFirst()
                .orElse(design.getImages().get(0));
        if (primary == null || primary.getImageName() == null) {
            return null;
        }
        String encoded = java.net.URLEncoder.encode(primary.getImageName(), StandardCharsets.UTF_8)
                .replace("+", "%20");
        return "/api/design-images/view/" + encoded;
    }
}
