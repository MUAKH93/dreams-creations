package com.dreams.dreamscreations.config;

import com.dreams.dreamscreations.tenant.MultiTenancyProperties;
import com.dreams.dreamscreations.tenant.TenantDataSourceProperties;
import com.dreams.dreamscreations.tenant.TenantDefinition;
import com.dreams.dreamscreations.tenant.TenantRoutingDataSource;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableConfigurationProperties(MultiTenancyProperties.class)
@ConditionalOnProperty(name = "app.multitenancy.enabled", havingValue = "true")
public class MultiTenantDataSourceConfiguration {

    @Bean
    @Primary
    public DataSource dataSource(MultiTenancyProperties multiTenancy) {
        Map<Object, Object> targets = new HashMap<>();
        for (Map.Entry<String, TenantDefinition> entry : multiTenancy.getTenants().entrySet()) {
            targets.put(entry.getKey(), buildPool(entry.getKey(), entry.getValue().getDatasource()));
        }

        if (targets.isEmpty()) {
            throw new IllegalStateException(
                    "app.multitenancy.enabled=true but no app.multitenancy.tenants.* configured");
        }

        String defaultId = multiTenancy.getDefaultTenantId();
        Object defaultDs = targets.get(defaultId);
        if (defaultDs == null) {
            throw new IllegalStateException(
                    "Default tenant '" + defaultId + "' is not defined in app.multitenancy.tenants");
        }

        TenantRoutingDataSource routing = new TenantRoutingDataSource();
        routing.setTargetDataSources(targets);
        routing.setDefaultTargetDataSource(defaultDs);
        routing.afterPropertiesSet();
        return routing;
    }

    private static HikariDataSource buildPool(String tenantId, TenantDataSourceProperties cfg) {
        if (cfg.getUrl() == null || cfg.getUrl().isBlank()) {
            throw new IllegalStateException("Missing JDBC url for tenant: " + tenantId);
        }
        HikariDataSource ds = new HikariDataSource();
        ds.setPoolName("tenant-" + tenantId);
        ds.setJdbcUrl(cfg.getUrl());
        ds.setUsername(cfg.getUsername());
        ds.setPassword(cfg.getPassword());
        ds.setDriverClassName(cfg.getDriverClassName());
        ds.setMaximumPoolSize(10);
        ds.setMinimumIdle(1);
        return ds;
    }
}
