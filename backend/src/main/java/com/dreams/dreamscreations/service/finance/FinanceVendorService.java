package com.dreams.dreamscreations.service.finance;

import com.dreams.dreamscreations.dto.finance.CreateFinanceVendorRequest;
import com.dreams.dreamscreations.dto.finance.FinanceVendorDTO;

import java.util.List;

public interface FinanceVendorService {

    List<FinanceVendorDTO> getAll(boolean activeOnly);

    FinanceVendorDTO getById(Long id);

    FinanceVendorDTO create(CreateFinanceVendorRequest request);

    FinanceVendorDTO update(Long id, CreateFinanceVendorRequest request);

    void deactivate(Long id);
}
