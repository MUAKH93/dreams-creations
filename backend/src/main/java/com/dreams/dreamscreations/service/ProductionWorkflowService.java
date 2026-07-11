package com.dreams.dreamscreations.service;

import com.dreams.dreamscreations.dto.ProductionStartRequest;
import com.dreams.dreamscreations.dto.ProductionStartResponse;

public interface ProductionWorkflowService {
    ProductionStartResponse startProductionOrder(ProductionStartRequest request);
}
