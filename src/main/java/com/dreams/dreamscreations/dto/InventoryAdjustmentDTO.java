package com.dreams.dreamscreations.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class InventoryAdjustmentDTO {
    private Long adjustmentId;
    private Long suitId;
    private String designCode;
    private String designName;
    private String sizeValue;
    private String color;
    private Integer previousQuantity;
    private Integer newQuantity;
    private String reason;
    private String adjustedByUsername;
    private LocalDateTime createdAt;
}
