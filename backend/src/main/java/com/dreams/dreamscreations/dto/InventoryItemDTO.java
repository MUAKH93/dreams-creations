package com.dreams.dreamscreations.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class InventoryItemDTO {
    private Long inventoryId;
    private Long suitId;
    private String designCode;
    private String designName;
    private String designStatus;
    private String categoryName;
    private String sizeValue;
    private String color;
    private Integer quantity;
    private String stockType;
    private LocalDateTime lastUpdated;
}
