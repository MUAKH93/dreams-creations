package com.dreams.dreamscreations.dto.finance;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FinanceVendorDTO {
    private Long vendorId;
    private String vendorName;
    private String phone;
    private String email;
    private String notes;
    private Boolean isActive;
}
