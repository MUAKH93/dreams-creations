package com.dreams.dreamscreations.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
@ConfigurationProperties(prefix = "app.bootstrap")
public class BootstrapProperties {

    /** Per-tenant factory admin seeded/updated on startup (tenant id → credentials). */
    private Map<String, TenantAdminSeed> tenantAdmins = new LinkedHashMap<>();

    @Data
    public static class TenantAdminSeed {
        private String username;
        private String password;
        private String email;
        private String firstName = "Admin";
        private String lastName = "User";
        /** Disable legacy admin usernames (e.g. admin) after seeding. */
        private boolean disableLegacyAdmins = true;
    }
}
