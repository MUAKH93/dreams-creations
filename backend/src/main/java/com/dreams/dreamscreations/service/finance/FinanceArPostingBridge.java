package com.dreams.dreamscreations.service.finance;

import com.dreams.dreamscreations.config.ModuleProperties;
import com.dreams.dreamscreations.entity.Bill;
import com.dreams.dreamscreations.entity.Payment;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

/**
 * Always-available bridge so operations services can trigger finance posting
 * without requiring the finance module beans to be loaded.
 */
@Service
public class FinanceArPostingBridge {

    private final ModuleProperties moduleProperties;
    private final ObjectProvider<FinanceArPostingService> postingService;

    public FinanceArPostingBridge(ModuleProperties moduleProperties,
                                  ObjectProvider<FinanceArPostingService> postingService) {
        this.moduleProperties = moduleProperties;
        this.postingService = postingService;
    }

    public void onBillCreated(Bill bill) {
        if (!isAutoPostEnabled()) {
            return;
        }
        postingService.ifAvailable(service -> service.postBillSale(bill));
    }

    public void onBillCancelled(Bill bill) {
        if (!isAutoPostEnabled()) {
            return;
        }
        postingService.ifAvailable(service -> service.voidBillSale(bill));
    }

    public void onPaymentRecorded(Payment payment) {
        if (!isAutoPostEnabled()) {
            return;
        }
        postingService.ifAvailable(service -> service.postPayment(payment));
    }

    private boolean isAutoPostEnabled() {
        return moduleProperties.getFinance().isEnabled()
                && moduleProperties.getFinance().isAutoPostAr();
    }
}
