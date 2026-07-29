package com.dreams.dreamscreations.dto.shop;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ShopCatalogDesignDTO {

    private Long designId;
    private String designCode;
    private String name;
    private String description;
    private BigDecimal basePrice;
    private boolean featured;
    private String categoryName;
    private String primaryImageUrl;
    private int totalStock;
    private List<ShopCatalogVariantDTO> variants = new ArrayList<>();

    public Long getDesignId() {
        return designId;
    }

    public void setDesignId(Long designId) {
        this.designId = designId;
    }

    public String getDesignCode() {
        return designCode;
    }

    public void setDesignCode(String designCode) {
        this.designCode = designCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }

    public boolean isFeatured() {
        return featured;
    }

    public void setFeatured(boolean featured) {
        this.featured = featured;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getPrimaryImageUrl() {
        return primaryImageUrl;
    }

    public void setPrimaryImageUrl(String primaryImageUrl) {
        this.primaryImageUrl = primaryImageUrl;
    }

    public int getTotalStock() {
        return totalStock;
    }

    public void setTotalStock(int totalStock) {
        this.totalStock = totalStock;
    }

    public List<ShopCatalogVariantDTO> getVariants() {
        return variants;
    }

    public void setVariants(List<ShopCatalogVariantDTO> variants) {
        this.variants = variants;
    }
}
