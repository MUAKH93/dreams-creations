package com.dreams.dreamscreations.tenant;

import org.springframework.stereotype.Component;

@Component
public class TenantExecutor {

    private final TenantRegistry tenantRegistry;

    public TenantExecutor(TenantRegistry tenantRegistry) {
        this.tenantRegistry = tenantRegistry;
    }

    public void forEachTenant(Runnable action) {
        if (!tenantRegistry.isEnabled()) {
            try {
                TenantContext.setTenantId(tenantRegistry.getDefaultTenantId());
                action.run();
            } finally {
                TenantContext.clear();
            }
            return;
        }
        for (String tenantId : tenantRegistry.getTenantIds()) {
            try {
                TenantContext.setTenantId(tenantId);
                action.run();
            } finally {
                TenantContext.clear();
            }
        }
    }
}
