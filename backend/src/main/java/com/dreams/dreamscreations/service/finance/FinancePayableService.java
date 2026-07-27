package com.dreams.dreamscreations.service.finance;

import com.dreams.dreamscreations.dto.finance.CreateFinancePayablePaymentRequest;
import com.dreams.dreamscreations.dto.finance.CreateFinancePayableRequest;
import com.dreams.dreamscreations.dto.finance.FinancePayableDTO;

import java.util.List;

public interface FinancePayableService {

    List<FinancePayableDTO> getAll();

    FinancePayableDTO getById(Long id);

    FinancePayableDTO create(CreateFinancePayableRequest request);

    FinancePayableDTO recordPayment(Long payableId, CreateFinancePayablePaymentRequest request);
}
