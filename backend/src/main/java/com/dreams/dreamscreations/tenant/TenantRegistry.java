package com.dreams.dreamscreations.tenant;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class TenantRegistry {

    private final MultiTenancyProperties properties;

    public TenantRegistry(MultiTenancyProperties properties) {
        this.properties = properties;
    }

    public boolean isEnabled() {
        return properties.isEnabled();
    }

    public String getDefaultTenantId() {
        return properties.getDefaultTenantId();
    }

    public boolean isValid(String tenantId) {
        if (tenantId == null || tenantId.isBlank()) {
            return false;
        }
        String id = tenantId.trim();
        if (!properties.isEnabled()) {
            return id.equals(properties.getDefaultTenantId());
        }
        return properties.getTenants().containsKey(id);
    }

    public Set<String> getTenantIds() {
        return properties.getTenants().keySet();
    }

    public String getDisplayName(String tenantId) {
        if (!properties.isEnabled()) {
            return "Dreams Creations";
        }
        TenantDefinition def = properties.getTenants().get(tenantId);
        if (def == null) {
            return tenantId;
        }
        if (def.getDisplayName() != null && !def.getDisplayName().isBlank()) {
            return def.getDisplayName().trim();
        }
        return tenantId;
    }

    public List<TenantInfo> listPublicTenants() {
        if (!properties.isEnabled()) {
            String id = properties.getDefaultTenantId();
            return List.of(new TenantInfo(id, getDisplayName(id)));
        }
        List<TenantInfo> list = new ArrayList<>();
        for (String id : properties.getTenants().keySet()) {
            list.add(new TenantInfo(id, getDisplayName(id)));
        }
        return list;
    }

    public record TenantInfo(String id, String displayName) {
    }
}
