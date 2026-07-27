package com.dreams.dreamscreations.dto.finance;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class InventoryValuationLineDTO {
    private Long suitId;
    private String designCode;
    private String designName;
    private String sizeValue;
    private String color;
    private int quantity;
    private BigDecimal unitCost;
    private BigDecimal lineValue;
}
