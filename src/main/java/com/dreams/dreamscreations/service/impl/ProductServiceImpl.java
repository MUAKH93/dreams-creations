package com.dreams.dreamscreations.service.impl;

import com.dreams.dreamscreations.dto.ProductStockDTO;
import com.dreams.dreamscreations.entity.Product;
import com.dreams.dreamscreations.entity.Suit;
import com.dreams.dreamscreations.repository.ProductRepository;
import com.dreams.dreamscreations.repository.SuitRepository;
import com.dreams.dreamscreations.service.InventoryService;
import com.dreams.dreamscreations.service.ProductService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepo;
    private final SuitRepository suitRepo;
    private final InventoryService inventoryService;

    public ProductServiceImpl(ProductRepository productRepo,
                              SuitRepository suitRepo,
                              InventoryService inventoryService) {
        this.productRepo = productRepo;
        this.suitRepo = suitRepo;
        this.inventoryService = inventoryService;
    }

    @Override public Product save(Product product) { return productRepo.save(product); }
    @Override public List<Product> getAll() { return productRepo.findAll(); }

    @Override
    @Transactional(readOnly = true)
    public List<ProductStockDTO> getAllWithStock() {
        return productRepo.findAllActiveWithSuitDetails().stream()
                .map(p -> ProductStockDTO.from(p, inventoryService.getQuantity(p.getSuit())))
                .toList();
    }

    @Override
    public Product getById(Long id) {
        return productRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found: " + id));
    }

    @Override
    public Product getBySuitId(Long suitId) {
        Suit suit = suitRepo.findById(suitId)
                .orElseThrow(() -> new RuntimeException("Suit not found: " + suitId));
        return productRepo.findBySuit(suit)
                .orElseThrow(() -> new RuntimeException("No product listing for suit: " + suitId));
    }

    @Override
    public Product update(Long id, Product updated) {
        Product existing = getById(id);
        existing.setSellingPrice(updated.getSellingPrice());
        existing.setStatus(updated.getStatus());
        return productRepo.save(existing);
    }
}
