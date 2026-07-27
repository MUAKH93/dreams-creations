package com.dreams.dreamscreations.service.finance;

import com.dreams.dreamscreations.config.ModuleProperties;
import com.dreams.dreamscreations.entity.Bill;
import com.dreams.dreamscreations.entity.ModuleAssignment;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

/**
 * Bridge so operations services can trigger inventory/COGS posting without
 * requiring finance module beans to be loaded.
 */
@Service
public class FinanceInventoryPostingBridge {

    private final ModuleProperties moduleProperties;
    private final ObjectProvider<FinanceInventoryPostingService> postingService;

    public FinanceInventoryPostingBridge(ModuleProperties moduleProperties,
                                         ObjectProvider<FinanceInventoryPostingService> postingService) {
        this.moduleProperties = moduleProperties;
        this.postingService = postingService;
    }

    public void onBillCreated(Bill bill) {
        if (!isAutoPostEnabled()) {
            return;
        }
        postingService.ifAvailable(service -> service.postBillCogs(bill));
    }

    public void onBillCancelled(Bill bill) {
        if (!isAutoPostEnabled()) {
            return;
        }
        postingService.ifAvailable(service -> service.voidBillCogs(bill));
    }

    public void onProductionReceipt(ModuleAssignment assignment) {
        if (!isAutoPostEnabled()) {
            return;
        }
        postingService.ifAvailable(service -> service.postProductionReceipt(assignment));
    }

    private boolean isAutoPostEnabled() {
        return moduleProperties.getFinance().isEnabled()
                && moduleProperties.getFinance().isAutoPostInventory();
    }
}
