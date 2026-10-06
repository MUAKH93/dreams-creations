package com.dreams.dreamscreations.tenant;

import lombok.Data;

@Data
public class TenantDefinition {
    private String displayName;
    private TenantDataSourceProperties datasource = new TenantDataSourceProperties();
}
