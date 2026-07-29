package com.dreams.dreamscreations.service.shop;

import com.dreams.dreamscreations.dto.shop.ShopCheckoutRequest;
import com.dreams.dreamscreations.dto.shop.ShopOrderDTO;

import java.util.List;

public interface ShopOrderService {

    ShopOrderDTO checkout(Long customerId, ShopCheckoutRequest request);

    List<ShopOrderDTO> getMyOrders(Long customerId);

    ShopOrderDTO getMyOrder(Long customerId, Long orderId);

    ShopOrderDTO cancelMyOrder(Long customerId, Long orderId);

    List<ShopOrderDTO> getAllOrders(String status);

    ShopOrderDTO getOrder(Long orderId);

    ShopOrderDTO updateStatus(Long orderId, String status);

    ShopOrderDTO convertToQuotation(Long orderId);

    ShopOrderDTO convertToBill(Long orderId);

    String generateNextOrderNumber();
}
