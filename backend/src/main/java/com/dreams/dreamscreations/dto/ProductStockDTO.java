package com.dreams.dreamscreations.dto;

import com.dreams.dreamscreations.entity.Product;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ProductStockDTO {
    private Long productId;
    private BigDecimal sellingPrice;
    private BigDecimal basePrice;
    private String status;
    private Long suitId;
    private Long designId;
    private Long sizeId;
    private String designCode;
    private String designName;
    private String sizeValue;
    private String color;
    private String categoryName;
    private Integer stockQuantity;

    public static ProductStockDTO from(Product product, int stockQuantity) {
        var suit = product.getSuit();
        var design = suit.getDesign();
        String sizeValue = suit.getSize() != null ? suit.getSize().getSizeValue() : "TBD";
        String categoryName = suit.getSize() != null && suit.getSize().getCategory() != null
                ? suit.getSize().getCategory().getCategoryName()
                : (design.getCategory() != null
                    ? design.getCategory().getCategoryName() : "—");
        BigDecimal basePrice = design.getBasePrice();
        BigDecimal sellingPrice = product.getSellingPrice();
        if (sellingPrice == null || sellingPrice.signum() <= 0) {
            sellingPrice = basePrice;
        }
        return ProductStockDTO.builder()
                .productId(product.getProductId())
                .sellingPrice(sellingPrice)
                .basePrice(basePrice)
                .status(product.getStatus())
                .suitId(suit.getSuitId())
                .designId(design.getDesignId())
                .sizeId(suit.getSize() != null ? suit.getSize().getSizeId() : null)
                .designCode(design.getDesignCode())
                .designName(design.getName())
                .sizeValue(sizeValue)
                .color(suit.getColor())
                .categoryName(categoryName)
                .stockQuantity(stockQuantity)
                .build();
    }
}
