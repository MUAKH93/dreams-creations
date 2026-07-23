package com.dreams.dreamscreations.service.finance;

import com.dreams.dreamscreations.dto.finance.CreateFinanceAccountRequest;
import com.dreams.dreamscreations.dto.finance.FinanceAccountDTO;
import com.dreams.dreamscreations.dto.finance.UpdateFinanceAccountRequest;

import java.util.List;

public interface FinanceAccountService {

    List<FinanceAccountDTO> getAll(boolean activeOnly);

    FinanceAccountDTO getById(Long id);

    FinanceAccountDTO create(CreateFinanceAccountRequest request);

    FinanceAccountDTO update(Long id, UpdateFinanceAccountRequest request);

    void delete(Long id);
}
