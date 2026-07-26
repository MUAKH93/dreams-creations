package com.dreams.dreamscreations.dto.finance;

import lombok.Data;

@Data
public class CreateFinanceAccountRequest {

    private String accountCode;
    private String accountName;
    private String accountType;
    private Long parentId;
    private String description;
}
