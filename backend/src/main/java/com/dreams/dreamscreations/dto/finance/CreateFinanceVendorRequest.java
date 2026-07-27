package com.dreams.dreamscreations.dto.finance;

import lombok.Data;

@Data
public class CreateFinanceVendorRequest {
    private String vendorName;
    private String phone;
    private String email;
    private String notes;
}
