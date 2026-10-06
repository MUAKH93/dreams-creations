package com.dreams.dreamscreations.security;

import com.dreams.dreamscreations.config.AppShopProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Validates X-Shop-Integration-Key for shop → ERP finance integration endpoints.
 */
@Component
public class ShopIntegrationAuthFilter extends OncePerRequestFilter {

    public static final String HEADER = "X-Shop-Integration-Key";

    private final AppShopProperties shopProperties;

    public ShopIntegrationAuthFilter(AppShopProperties shopProperties) {
        this.shopProperties = shopProperties;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path == null || !path.startsWith("/api/integration/shop");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String configured = shopProperties.getIntegration().getApiKey();
        if (configured == null || configured.isBlank()) {
            response.sendError(HttpStatus.SERVICE_UNAVAILABLE.value(),
                    "Shop integration is not configured (app.shop.integration.api-key)");
            return;
        }

        String provided = request.getHeader(HEADER);
        if (provided == null || !configured.equals(provided)) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Invalid shop integration key");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
