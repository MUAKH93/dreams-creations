package com.dreams.dreamscreations.service.finance;

import com.dreams.dreamscreations.entity.Bill;
import com.dreams.dreamscreations.entity.ModuleAssignment;

public interface FinanceInventoryPostingService {

    void postBillCogs(Bill bill);

    void voidBillCogs(Bill bill);

    void postProductionReceipt(ModuleAssignment assignment);
}
