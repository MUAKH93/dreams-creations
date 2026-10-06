package com.dreams.dreamscreations.config;

import com.dreams.dreamscreations.service.TenantAdminBootstrapService;
import com.dreams.dreamscreations.tenant.TenantContext;
import com.dreams.dreamscreations.tenant.TenantExecutor;
import com.dreams.dreamscreations.tenant.TenantRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class TenantAdminBootstrapRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(TenantAdminBootstrapRunner.class);
    private final BootstrapProperties bootstrapProperties;
    private final TenantRegistry tenantRegistry;
    private final TenantExecutor tenantExecutor;
    private final TenantAdminBootstrapService bootstrapService;

    public TenantAdminBootstrapRunner(BootstrapProperties bootstrapProperties,
                                      TenantRegistry tenantRegistry,
                                      TenantExecutor tenantExecutor,
                                      TenantAdminBootstrapService bootstrapService) {
        this.bootstrapProperties = bootstrapProperties;
        this.tenantRegistry = tenantRegistry;
        this.tenantExecutor = tenantExecutor;
        this.bootstrapService = bootstrapService;
    }

    @Override
    public void run(ApplicationArguments args) {
        Map<String, BootstrapProperties.TenantAdminSeed> seeds = bootstrapProperties.getTenantAdmins();
        if (seeds == null || seeds.isEmpty()) {
            return;
        }

        tenantExecutor.forEachTenant(() -> {
            String tenantId = TenantContext.getTenantId();
            BootstrapProperties.TenantAdminSeed seed = seeds.get(tenantId);
            if (seed == null || isBlank(seed.getUsername()) || isBlank(seed.getPassword())) {
                return;
            }
            try {
                bootstrapService.upsertTenantAdmin(seed);
                log.info("Tenant [{}]: ensured admin user '{}'", tenantId, seed.getUsername());
            } catch (Exception e) {
                log.error("Tenant [{}]: admin bootstrap failed: {}", tenantId, e.getMessage());
            }
        });
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
