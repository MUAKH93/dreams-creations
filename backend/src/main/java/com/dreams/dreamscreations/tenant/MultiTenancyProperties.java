package com.dreams.dreamscreations.tenant;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
@ConfigurationProperties(prefix = "app.multitenancy")
public class MultiTenancyProperties {

    private boolean enabled = false;
    private String defaultTenantId = "default";
    private Map<String, TenantDefinition> tenants = new LinkedHashMap<>();
}
