package com.dreams.dreamscreations.dto.finance;

import lombok.Data;

@Data
public class UpdateFinanceAccountRequest {

    private String accountName;
    private String accountType;
    private Long parentId;
    private Boolean isActive;
    private String description;
}
