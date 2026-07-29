package com.dreams.dreamscreations.service.shop;

import com.dreams.dreamscreations.dto.shop.ShopCatalogDesignDTO;

import java.util.List;

public interface ShopCatalogService {

    List<ShopCatalogDesignDTO> getCatalog(Boolean featuredOnly);

    ShopCatalogDesignDTO getDesign(Long designId);
}
