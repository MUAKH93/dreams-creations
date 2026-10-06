package com.dreams.dreamscreations.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.shop")
public class AppShopProperties {

    private Integration integration = new Integration();

    public Integration getIntegration() {
        return integration;
    }

    public void setIntegration(Integration integration) {
        this.integration = integration;
    }

    public static class Integration {
        /** Shared secret — shop backend sends X-Shop-Integration-Key header. */
        private String apiKey = "";

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }
    }
}
