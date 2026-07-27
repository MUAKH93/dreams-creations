package com.dreams.dreamscreations.service.finance;

import com.dreams.dreamscreations.entity.finance.FinancePayable;
import com.dreams.dreamscreations.entity.finance.FinancePayablePayment;

public interface FinanceApPostingService {

    void postPayableInvoice(FinancePayable payable);

    void postPayablePayment(FinancePayablePayment payment);
}
