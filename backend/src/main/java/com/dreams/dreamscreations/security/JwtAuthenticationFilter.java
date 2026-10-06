package com.dreams.dreamscreations.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import com.dreams.dreamscreations.service.PlatformAuthService;
import com.dreams.dreamscreations.tenant.TenantContext;
import com.dreams.dreamscreations.tenant.TenantRegistry;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;
    private final TenantRegistry tenantRegistry;
    private final PlatformAuthService platformAuthService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil,
                                   UserDetailsServiceImpl userDetailsService,
                                   TenantRegistry tenantRegistry,
                                   PlatformAuthService platformAuthService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.tenantRegistry = tenantRegistry;
        this.platformAuthService = platformAuthService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        // No token — let Spring Security handle public vs protected routes
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {
            String role = jwtUtil.extractRole(token);
            String username = jwtUtil.extractUsername(token);

            if (PlatformAuthService.ROLE_SUPER_ADMIN.equals(role)
                    && username != null
                    && platformAuthService.isPlatformSuperAdmin(username)
                    && SecurityContextHolder.getContext().getAuthentication() == null) {

                if (jwtUtil.validateToken(token, buildPlatformUserDetails(username), null)) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    buildPlatformUserDetails(username), null,
                                    buildPlatformUserDetails(username).getAuthorities());
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    filterChain.doFilter(request, response);
                    return;
                }
                rejectToken(response, "Invalid or expired platform token.");
                return;
            }

            String tenantFromToken = jwtUtil.extractTenantId(token);
            if (tenantFromToken == null || tenantFromToken.isBlank()) {
                tenantFromToken = tenantRegistry.getDefaultTenantId();
            }
            if (!tenantRegistry.isValid(tenantFromToken)) {
                rejectToken(response, "Invalid tenant in token.");
                return;
            }

            String headerTenant = request.getHeader(TenantContextFilter.HEADER);
            if (headerTenant != null && !headerTenant.isBlank()
                    && !headerTenant.trim().equals(tenantFromToken)) {
                rejectToken(response, "Tenant header does not match token.");
                return;
            }

            TenantContext.setTenantId(tenantFromToken);

            if (username != null &&
                    SecurityContextHolder.getContext().getAuthentication() == null) {

                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                if (jwtUtil.validateToken(token, userDetails, tenantFromToken)) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    filterChain.doFilter(request, response);
                    return;
                }
            }
        } catch (Exception e) {
            log.warn("JWT rejected for {}: {}", request.getRequestURI(), e.getMessage());
        }

        rejectToken(response, "Invalid or expired token. Please login again.");
    }

    private UserDetails buildPlatformUserDetails(String username) {
        return org.springframework.security.core.userdetails.User.builder()
                .username(username)
                .password("")
                .authorities("ROLE_" + PlatformAuthService.ROLE_SUPER_ADMIN)
                .build();
    }

    private void rejectToken(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"message\":\"" + message.replace("\"", "\\\"") + "\"}");
    }
}
