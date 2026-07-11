package com.dreams.dreamscreations.dto;

import lombok.Data;

@Data
public class InventoryAdjustRequest {
    private Integer newQuantity;
    private String reason;
}
