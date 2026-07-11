package com.dreams.dreamscreations.service.impl;

import com.dreams.dreamscreations.dto.InventoryAdjustmentDTO;
import com.dreams.dreamscreations.dto.InventoryItemDTO;
import com.dreams.dreamscreations.entity.Alert;
import com.dreams.dreamscreations.entity.Inventory;
import com.dreams.dreamscreations.entity.InventoryAdjustment;
import com.dreams.dreamscreations.entity.Product;
import com.dreams.dreamscreations.entity.Suit;
import com.dreams.dreamscreations.entity.User;
import com.dreams.dreamscreations.repository.AlertRepository;
import com.dreams.dreamscreations.repository.InventoryAdjustmentRepository;
import com.dreams.dreamscreations.repository.InventoryRepository;
import com.dreams.dreamscreations.repository.ProductRepository;
import com.dreams.dreamscreations.repository.SuitRepository;
import com.dreams.dreamscreations.service.InventoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class InventoryServiceImpl implements InventoryService {

    public static final int LOW_STOCK_THRESHOLD = 5;

    private final InventoryRepository inventoryRepo;
    private final SuitRepository suitRepo;
    private final ProductRepository productRepo;
    private final AlertRepository alertRepo;
    private final InventoryAdjustmentRepository adjustmentRepo;

    public InventoryServiceImpl(InventoryRepository inventoryRepo,
                                SuitRepository suitRepo,
                                ProductRepository productRepo,
                                AlertRepository alertRepo,
                                InventoryAdjustmentRepository adjustmentRepo) {
        this.inventoryRepo = inventoryRepo;
        this.suitRepo = suitRepo;
        this.productRepo = productRepo;
        this.alertRepo = alertRepo;
        this.adjustmentRepo = adjustmentRepo;
    }

    @Override
    @Transactional
    public Inventory addStock(Suit suit, int quantity) {
        if (quantity <= 0) return inventoryRepo.findBySuit(suit).orElse(null);

        Inventory inventory = inventoryRepo.findBySuit(suit)
                .orElse(Inventory.builder()
                        .suit(suit)
                        .quantity(0)
                        .stockType("finished_goods")
                        .build());

        inventory.setQuantity(inventory.getQuantity() + quantity);
        Inventory saved = inventoryRepo.save(inventory);
        ensureProductListing(suit);
        syncLowStockAlerts();
        return saved;
    }

    @Override
    @Transactional
    public Inventory removeStock(Suit suit, int quantity) {
        if (quantity <= 0) return inventoryRepo.findBySuit(suit).orElse(null);

        Inventory inventory = inventoryRepo.findBySuit(suit)
                .orElseThrow(() -> new RuntimeException(stockLabel(suit) + " — no stock record (0 available)"));

        if (inventory.getQuantity() < quantity) {
            throw new RuntimeException(stockLabel(suit)
                    + " — insufficient stock: only " + inventory.getQuantity()
                    + " available, tried to sell " + quantity);
        }

        inventory.setQuantity(inventory.getQuantity() - quantity);
        Inventory saved = inventoryRepo.save(inventory);
        syncLowStockAlerts();
        return saved;
    }

    @Override
    @Transactional
    public Inventory restoreStock(Suit suit, int quantity) {
        if (quantity <= 0) return inventoryRepo.findBySuit(suit).orElse(null);
        return addStock(suit, quantity);
    }

    @Override
    @Transactional(readOnly = true)
    public int getQuantity(Suit suit) {
        return inventoryRepo.findBySuit(suit).map(Inventory::getQuantity).orElse(0);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryItemDTO> getAllStock() {
        return inventoryRepo.findAllWithDetails().stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryItemDTO getBySuitId(Long suitId) {
        Suit suit = suitRepo.findById(suitId)
                .orElseThrow(() -> new RuntimeException("Suit not found: " + suitId));
        return inventoryRepo.findBySuit(suit)
                .map(this::toDto)
                .orElse(InventoryItemDTO.builder()
                        .suitId(suitId)
                        .designCode(suit.getDesign().getDesignCode())
                        .designName(suit.getDesign().getName())
                        .categoryName(categoryNameFor(suit))
                        .sizeValue(suit.getSize() != null ? suit.getSize().getSizeValue() : "TBD")
                        .color(suit.getColor())
                        .quantity(0)
                        .stockType("finished_goods")
                        .build());
    }

    @Override
    @Transactional
    public InventoryItemDTO adjustStock(Long suitId, int newQuantity, String reason, User adjustedBy) {
        if (newQuantity < 0) {
            throw new RuntimeException("Quantity cannot be negative");
        }
        if (reason == null || reason.isBlank()) {
            throw new RuntimeException("Reason is required for stock adjustment");
        }

        Suit suit = suitRepo.findById(suitId)
                .orElseThrow(() -> new RuntimeException("Suit not found: " + suitId));
        int previous = getQuantity(suit);

        Inventory inventory = inventoryRepo.findBySuit(suit)
                .orElse(Inventory.builder()
                        .suit(suit)
                        .quantity(0)
                        .stockType("finished_goods")
                        .build());
        inventory.setQuantity(newQuantity);
        inventoryRepo.save(inventory);

        adjustmentRepo.save(InventoryAdjustment.builder()
                .suit(suit)
                .previousQuantity(previous)
                .newQuantity(newQuantity)
                .reason(reason.trim())
                .adjustedBy(adjustedBy)
                .build());

        if (newQuantity > 0) {
            ensureProductListing(suit);
        }
        syncLowStockAlerts();
        return getBySuitId(suitId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryAdjustmentDTO> getAdjustmentHistory() {
        return adjustmentRepo.findAllWithDetails().stream()
                .map(this::toAdjustmentDto)
                .toList();
    }

    private InventoryAdjustmentDTO toAdjustmentDto(InventoryAdjustment adj) {
        Suit suit = adj.getSuit();
        return InventoryAdjustmentDTO.builder()
                .adjustmentId(adj.getAdjustmentId())
                .suitId(suit.getSuitId())
                .designCode(suit.getDesign().getDesignCode())
                .designName(suit.getDesign().getName())
                .sizeValue(suit.getSize() != null ? suit.getSize().getSizeValue() : "TBD")
                .color(suit.getColor())
                .previousQuantity(adj.getPreviousQuantity())
                .newQuantity(adj.getNewQuantity())
                .reason(adj.getReason())
                .adjustedByUsername(adj.getAdjustedBy() != null ? adj.getAdjustedBy().getUsername() : null)
                .createdAt(adj.getCreatedAt())
                .build();
    }

    private void ensureProductListing(Suit suit) {
        productRepo.findBySuit(suit).orElseGet(() -> {
            BigDecimal price = suit.getDesign().getBasePrice();
            if (price == null || price.signum() <= 0) {
                price = BigDecimal.ZERO;
            }
            return productRepo.save(Product.builder()
                    .suit(suit)
                    .sellingPrice(price)
                    .status("active")
                    .build());
        });
    }

    private String stockLabel(Suit suit) {
        String size = suit.getSize() != null ? suit.getSize().getSizeValue() : "TBD";
        return suit.getDesign().getDesignCode() + " / " + size + " / " + suit.getColor();
    }

    private String categoryNameFor(Suit suit) {
        if (suit.getSize() != null && suit.getSize().getCategory() != null) {
            return suit.getSize().getCategory().getCategoryName();
        }
        if (suit.getDesign() != null && suit.getDesign().getCategory() != null) {
            return suit.getDesign().getCategory().getCategoryName();
        }
        return "—";
    }

    private InventoryItemDTO toDto(Inventory inv) {
        Suit suit = inv.getSuit();
        return InventoryItemDTO.builder()
                .inventoryId(inv.getInventoryId())
                .suitId(suit.getSuitId())
                .designCode(suit.getDesign().getDesignCode())
                .designName(suit.getDesign().getName())
                .categoryName(categoryNameFor(suit))
                .sizeValue(suit.getSize() != null ? suit.getSize().getSizeValue() : "TBD")
                .color(suit.getColor())
                .quantity(inv.getQuantity())
                .stockType(inv.getStockType())
                .lastUpdated(inv.getLastUpdated())
                .build();
    }

    @Override
    @Transactional
    public void syncLowStockAlerts() {
        inventoryRepo.findAllWithDetails().forEach(inv -> {
            Suit suit = inv.getSuit();
            int qty = inv.getQuantity();
            Long suitId = suit.getSuitId();
            boolean isLow = qty <= LOW_STOCK_THRESHOLD;

            var openAlerts = alertRepo.findByRelatedEntityTypeAndRelatedEntityId("SUIT", suitId).stream()
                    .filter(a -> "open".equals(a.getStatus()) && "LOW_STOCK".equals(a.getAlertType()))
                    .toList();

            if (isLow) {
                String msg = "Low stock: " + stockLabel(suit)
                        + " — only " + qty + " left (threshold ≤ " + LOW_STOCK_THRESHOLD + ")";
                if (openAlerts.isEmpty()) {
                    alertRepo.save(Alert.builder()
                            .alertType("LOW_STOCK")
                            .message(msg)
                            .relatedEntityType("SUIT")
                            .relatedEntityId(suitId)
                            .status("open")
                            .build());
                } else {
                    Alert existing = openAlerts.get(0);
                    existing.setMessage(msg);
                    alertRepo.save(existing);
                }
            } else {
                openAlerts.forEach(a -> {
                    a.setStatus("resolved");
                    a.setResolvedDate(LocalDateTime.now());
                    alertRepo.save(a);
                });
            }
        });
    }
}
