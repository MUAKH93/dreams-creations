package com.dreams.dreamscreations.config;

import com.dreams.dreamscreations.security.JwtAuthenticationFilter;
import com.dreams.dreamscreations.security.UserDetailsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;
    private final JwtAuthenticationFilter jwtFilter;

    public SecurityConfig(UserDetailsServiceImpl userDetailsService,
                          JwtAuthenticationFilter jwtFilter) {
        this.userDetailsService = userDetailsService;
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Explicitly build the AuthenticationManager with our UserDetailsService
     * and PasswordEncoder wired together — no ambiguity about which encoder
     * Spring Security uses for comparison.
     */
    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return new ProviderManager(provider);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/health", "/api/health/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/designs", "/api/designs/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/categories", "/api/categories/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/sizes", "/api/sizes/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/products", "/api/products/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/design-types", "/api/design-types/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/categories", "/api/categories/**").hasAnyRole("ADMIN","MANAGER")
                .requestMatchers(HttpMethod.POST, "/api/design-types", "/api/design-types/**").hasAnyRole("ADMIN","MANAGER")
                .requestMatchers(HttpMethod.GET, "/api/design-images/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/design-images", "/api/design-images/**").hasAnyRole("ADMIN","MANAGER")
                .requestMatchers(HttpMethod.PUT, "/api/design-images", "/api/design-images/**").hasAnyRole("ADMIN","MANAGER")
                .requestMatchers(HttpMethod.DELETE, "/api/design-images", "/api/design-images/**").hasAnyRole("ADMIN","MANAGER")
                // Manager + Admin — sales & production management
                .requestMatchers(HttpMethod.GET, "/api/bills/my").hasAnyRole("CUSTOMER","ADMIN","MANAGER")
                .requestMatchers(HttpMethod.GET, "/api/bills", "/api/bills/**").hasAnyRole("MANAGER","ADMIN","CUSTOMER")
                .requestMatchers(HttpMethod.POST, "/api/bills", "/api/bills/**").hasAnyRole("MANAGER","ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/bills", "/api/bills/**").hasAnyRole("MANAGER","ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/customers/me/balance").hasAnyRole("CUSTOMER","ADMIN","MANAGER")
                .requestMatchers(HttpMethod.GET, "/api/customers", "/api/customers/**").hasAnyRole("MANAGER","ADMIN","CUSTOMER")
                .requestMatchers(HttpMethod.POST, "/api/customers", "/api/customers/**").hasAnyRole("MANAGER","ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/customers", "/api/customers/**").hasAnyRole("MANAGER","ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/customers", "/api/customers/**").hasAnyRole("MANAGER","ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/payments", "/api/payments/**").hasAnyRole("MANAGER","ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/payments", "/api/payments/**").hasAnyRole("MANAGER","ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/payment-methods", "/api/payment-methods/**").hasAnyRole("MANAGER","ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/production-batches", "/api/production-batches/**").hasAnyRole("MANAGER","ADMIN","SUPERVISOR")
                .requestMatchers(HttpMethod.POST, "/api/production-batches", "/api/production-batches/**").hasAnyRole("MANAGER","ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/production-batches/*").hasAnyRole("MANAGER","ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/production-batches/*/cancel").hasAnyRole("MANAGER","ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/production/start-order").hasAnyRole("MANAGER","ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/module-assignments/*/return").hasAnyRole("SUPERVISOR","MANAGER","ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/module-assignments").hasAnyRole("MANAGER","ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/module-assignments/mine").hasAnyRole("SUPERVISOR","MANAGER","ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/module-assignments", "/api/module-assignments/**").hasAnyRole("MANAGER","ADMIN","SUPERVISOR")
                .requestMatchers(HttpMethod.GET, "/api/production-flow", "/api/production-flow/**").hasAnyRole("MANAGER","ADMIN","SUPERVISOR")
                .requestMatchers(HttpMethod.GET, "/api/alerts", "/api/alerts/**").hasAnyRole("MANAGER","ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/alerts", "/api/alerts/**").hasAnyRole("MANAGER","ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/designs", "/api/designs/**").hasAnyRole("MANAGER","ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/designs/*").hasAnyRole("MANAGER","ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/designs", "/api/designs/**").hasAnyRole("MANAGER","ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/suits", "/api/suits/**").hasAnyRole("MANAGER","ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/suits", "/api/suits/**").hasAnyRole("MANAGER","ADMIN")
                // Admin-only setup
                .requestMatchers("/api/production-stages", "/api/production-stages/**").hasAnyRole("ADMIN","MANAGER")
                .requestMatchers("/api/production-modules", "/api/production-modules/**").hasAnyRole("ADMIN","MANAGER")
                .requestMatchers("/api/supervisors", "/api/supervisors/**").hasAnyRole("ADMIN","MANAGER")
                .requestMatchers(HttpMethod.GET, "/api/admin/supervisor-accounts", "/api/admin/supervisor-accounts/**")
                    .hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/admin/supervisor-accounts", "/api/admin/supervisor-accounts/**")
                    .hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/admin/supervisor-accounts", "/api/admin/supervisor-accounts/**")
                    .hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/admin/supervisor-accounts", "/api/admin/supervisor-accounts/**")
                    .hasRole("ADMIN")
                .requestMatchers("/api/admin/manager-accounts", "/api/admin/manager-accounts/**")
                    .hasRole("ADMIN")
                .requestMatchers("/api/supervisor-modules", "/api/supervisor-modules/**")
                    .hasAnyRole("ADMIN","MANAGER")
                .requestMatchers(HttpMethod.POST, "/api/sizes", "/api/sizes/**").hasAnyRole("ADMIN","MANAGER")
                .requestMatchers(HttpMethod.POST, "/api/payment-methods", "/api/payment-methods/**")
                    .hasAnyRole("ADMIN","MANAGER")
                .requestMatchers(HttpMethod.GET, "/api/designing-work-types", "/api/designing-work-types/**")
                    .hasAnyRole("ADMIN","MANAGER")
                .requestMatchers(HttpMethod.POST, "/api/designing-work-types", "/api/designing-work-types/**")
                    .hasAnyRole("ADMIN","MANAGER")
                .requestMatchers(HttpMethod.GET, "/api/filling-work-types", "/api/filling-work-types/**")
                    .hasAnyRole("ADMIN","MANAGER")
                .requestMatchers(HttpMethod.POST, "/api/filling-work-types", "/api/filling-work-types/**")
                    .hasAnyRole("ADMIN","MANAGER")
                .requestMatchers(HttpMethod.GET, "/api/inventory", "/api/inventory/**").hasAnyRole("ADMIN","MANAGER")
                .requestMatchers(HttpMethod.POST, "/api/inventory", "/api/inventory/**").hasAnyRole("ADMIN","MANAGER")
                .requestMatchers(HttpMethod.GET, "/api/dashboard", "/api/dashboard/**").hasAnyRole("ADMIN","MANAGER")
                .requestMatchers(HttpMethod.GET, "/api/analytics", "/api/analytics/**").hasAnyRole("ADMIN","MANAGER")
                .requestMatchers(HttpMethod.GET, "/api/quotations/my").hasAnyRole("CUSTOMER","ADMIN","MANAGER")
                .requestMatchers(HttpMethod.GET, "/api/quotations/next-number").hasAnyRole("ADMIN","MANAGER")
                .requestMatchers(HttpMethod.GET, "/api/quotations/*").hasAnyRole("CUSTOMER","ADMIN","MANAGER")
                .requestMatchers(HttpMethod.GET, "/api/quotations").hasAnyRole("ADMIN","MANAGER")
                .requestMatchers(HttpMethod.POST, "/api/quotations").hasAnyRole("CUSTOMER","ADMIN","MANAGER")
                .requestMatchers(HttpMethod.PUT, "/api/quotations/*").hasAnyRole("CUSTOMER","ADMIN","MANAGER")
                .requestMatchers(HttpMethod.POST, "/api/quotations/*/submit").hasAnyRole("CUSTOMER","ADMIN","MANAGER")
                .requestMatchers(HttpMethod.PATCH, "/api/quotations/*/status").hasAnyRole("ADMIN","MANAGER")
                .requestMatchers(HttpMethod.POST, "/api/quotations/*/convert-to-bill").hasAnyRole("ADMIN","MANAGER")
                .requestMatchers(HttpMethod.GET, "/api/activity-log", "/api/activity-log/**").hasAnyRole("ADMIN","MANAGER")
                .requestMatchers(HttpMethod.GET, "/api/customers/payment-reminders").hasAnyRole("ADMIN","MANAGER")
                .requestMatchers(HttpMethod.PUT, "/api/products", "/api/products/**").hasAnyRole("ADMIN","MANAGER")
                .requestMatchers(HttpMethod.PUT, "/api/designs/*/required-stages").hasAnyRole("ADMIN","MANAGER")
                // Everything else needs auth
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
                "http://localhost:3000",
                "http://localhost:5173"
        ));
        config.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
