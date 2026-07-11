package com.dreams.dreamscreations.service;

import com.dreams.dreamscreations.dto.InventoryAdjustmentDTO;
import com.dreams.dreamscreations.dto.InventoryItemDTO;
import com.dreams.dreamscreations.entity.Inventory;
import com.dreams.dreamscreations.entity.Suit;
import com.dreams.dreamscreations.entity.User;

import java.util.List;

public interface InventoryService {
    Inventory addStock(Suit suit, int quantity);
    Inventory removeStock(Suit suit, int quantity);
    Inventory restoreStock(Suit suit, int quantity);
    int getQuantity(Suit suit);
    List<InventoryItemDTO> getAllStock();
    InventoryItemDTO getBySuitId(Long suitId);
    InventoryItemDTO adjustStock(Long suitId, int newQuantity, String reason, User adjustedBy);
    List<InventoryAdjustmentDTO> getAdjustmentHistory();
    void syncLowStockAlerts();
}
