package com.dreams.dreamscreations.security;

import com.dreams.dreamscreations.tenant.TenantContext;
import com.dreams.dreamscreations.tenant.TenantRegistry;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Resolves tenant before JPA / auth touch the database.
 * Authenticated requests should send {@link #HEADER}; JWT also carries tenantId (validated in JwtAuthenticationFilter).
 */
@Component
public class TenantContextFilter extends OncePerRequestFilter {

    public static final String HEADER = "X-Tenant-ID";
    public static final String QUERY_PARAM = "tenant";

    private static final Pattern TENANT_ID_JSON = Pattern.compile(
            "\"tenantId\"\\s*:\\s*\"([^\"]+)\"");

    private final TenantRegistry tenantRegistry;

    public TenantContextFilter(TenantRegistry tenantRegistry) {
        this.tenantRegistry = tenantRegistry;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }
        String path = request.getRequestURI();
        return "/api/auth/tenants".equals(path)
                || "/api/auth/platform/login".equals(path);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        HttpServletRequest active = request;
        if (isAuthBodyTenantPath(request)) {
            active = new ContentCachingRequestWrapper(request, 8192);
        }

        try {
            if (hasBearerToken(active)) {
                filterChain.doFilter(active, response);
                return;
            }

            String tenantId = resolveTenant(active);
            if (tenantId == null) {
                writeBadRequest(response,
                        "Missing tenant. Send header " + HEADER + " or query param " + QUERY_PARAM + ".");
                return;
            }
            if (!tenantRegistry.isValid(tenantId)) {
                writeBadRequest(response, "Unknown tenant: " + tenantId);
                return;
            }

            TenantContext.setTenantId(tenantId);
            filterChain.doFilter(active, response);
        } finally {
            TenantContext.clear();
        }
    }

    private boolean hasBearerToken(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        return auth != null && auth.startsWith("Bearer ");
    }

    private String resolveTenant(HttpServletRequest request) throws IOException {
        String fromHeader = trimToNull(request.getHeader(HEADER));
        if (fromHeader != null) {
            return fromHeader;
        }

        String fromQuery = trimToNull(request.getParameter(QUERY_PARAM));
        if (fromQuery != null) {
            return fromQuery;
        }

        if (request instanceof ContentCachingRequestWrapper cached) {
            String fromBody = readTenantFromCachedBody(cached);
            if (fromBody != null) {
                return fromBody;
            }
        }

        if (!tenantRegistry.isEnabled()) {
            return tenantRegistry.getDefaultTenantId();
        }

        if ("/api/health".equals(request.getRequestURI())) {
            return tenantRegistry.getDefaultTenantId();
        }

        return null;
    }

    private boolean isAuthBodyTenantPath(HttpServletRequest request) {
        if (!HttpMethod.POST.matches(request.getMethod())) {
            return false;
        }
        String path = request.getRequestURI();
        return "/api/auth/login".equals(path)
                || "/api/auth/register".equals(path)
                || "/api/auth/forgot-password".equals(path)
                || "/api/auth/resend-verification".equals(path);
    }

    private String readTenantFromCachedBody(ContentCachingRequestWrapper request) throws IOException {
        request.getInputStream().readAllBytes();
        byte[] buf = request.getContentAsByteArray();
        if (buf.length == 0) {
            return null;
        }
        String body = new String(buf, StandardCharsets.UTF_8);
        Matcher matcher = TENANT_ID_JSON.matcher(body);
        if (matcher.find()) {
            return trimToNull(matcher.group(1));
        }
        return null;
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String t = value.trim();
        return t.isEmpty() ? null : t;
    }

    private void writeBadRequest(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"message\":\"" + message.replace("\"", "\\\"") + "\"}");
    }
}
