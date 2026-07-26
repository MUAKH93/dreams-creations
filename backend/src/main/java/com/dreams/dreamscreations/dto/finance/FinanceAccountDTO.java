package com.dreams.dreamscreations.dto.finance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinanceAccountDTO {

    private Long accountId;
    private String accountCode;
    private String accountName;
    private String accountType;
    private Long parentId;
    private String parentCode;
    private Boolean isActive;
    private Boolean isSystem;
    private String description;
}
