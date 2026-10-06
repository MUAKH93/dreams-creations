package com.dreams.dreamscreations.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({ModuleProperties.class, AppShopProperties.class, com.dreams.dreamscreations.tenant.MultiTenancyProperties.class})
public class ModuleConfiguration {
}
