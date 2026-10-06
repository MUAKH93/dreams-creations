package com.dreams.dreamscreations.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app.platform")
public class PlatformProperties {

    private SuperAdmin superAdmin = new SuperAdmin();

    @Data
    public static class SuperAdmin {
        /** Platform login username (not stored in tenant databases). */
        private String username = "RovexaPlatformSuperAdmin";
        /** Plain password — set in application.properties only (file is gitignored). */
        private String password = "";
        private boolean enabled = true;
    }
}
