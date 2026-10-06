package com.dreams.dreamscreations.service.integration;

import com.dreams.dreamscreations.dto.integration.ShopOrderSyncRequest;
import com.dreams.dreamscreations.dto.integration.ShopOrderSyncResponse;

public interface ShopFinanceIntegrationService {

    ShopOrderSyncResponse syncShopOrder(ShopOrderSyncRequest request);
}
