package com.dreams.dreamscreations.service;

import com.dreams.dreamscreations.dto.ProductStockDTO;
import com.dreams.dreamscreations.entity.Product;
import java.util.List;

public interface ProductService {
    Product save(Product product);
    List<Product> getAll();
    List<ProductStockDTO> getAllWithStock();
    Product getById(Long id);
    Product getBySuitId(Long suitId);
    Product update(Long id, Product product);
}
