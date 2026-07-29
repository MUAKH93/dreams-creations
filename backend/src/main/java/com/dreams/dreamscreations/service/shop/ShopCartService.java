package com.dreams.dreamscreations.service.shop;

import com.dreams.dreamscreations.dto.shop.ShopCartDTO;
import com.dreams.dreamscreations.dto.shop.ShopCartItemRequest;

import java.util.List;

public interface ShopCartService {

    ShopCartDTO getCart(Long customerId);

    ShopCartDTO addItem(Long customerId, Long productId, int quantity);

    ShopCartDTO updateItemQuantity(Long customerId, Long itemId, int quantity);

    ShopCartDTO removeItem(Long customerId, Long itemId);

    ShopCartDTO clearCart(Long customerId);

    ShopCartDTO mergeGuestItems(Long customerId, List<ShopCartItemRequest> guestItems);
}
