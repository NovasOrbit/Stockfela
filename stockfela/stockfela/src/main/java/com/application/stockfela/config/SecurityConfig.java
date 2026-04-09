package com.application.stockfela.config;

import com.application.stockfela.JWT.AuthEntryPointJWT;
import com.application.stockfela.JWT.AuthTokenFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
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

import java.util.Arrays;
import java.util.List;

/**
 * Central Spring Security configuration for Stockfela.
 *
 * <p>Key decisions:
 * <ul>
 *   <li>Stateless session – JWT is the only authentication mechanism.</li>
 *   <li>CSRF disabled – safe for stateless REST APIs using Bearer tokens.</li>
 *   <li>Only {@code /api/auth/**} and dev tooling endpoints are public;
 *       everything else requires a valid JWT.</li>
 *   <li>CORS origins are driven by the {@code ALLOWED_ORIGINS} env var so
 *       no wildcard (*) origins are ever permitted in production.</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    /** Handles 401 responses when a request arrives without a valid JWT. */
    @Autowired
    private AuthEntryPointJWT unauthorizedHandler;

    /**
     * Comma-separated list of allowed CORS origins, e.g.
     * {@code http://localhost:5174,https://app.stockfela.com}.
     * Defaults to localhost dev server.
     */
    @Value("${ALLOWED_ORIGINS:http://localhost:5174}")
    private String allowedOrigins;

    // ── Beans ──────────────────────────────────────────────────────────────

    /**
     * Creates the JWT authentication filter that runs before every request.
     * Declared as a bean so Spring can manage its lifecycle and injected
     * dependencies ({@link com.application.stockfela.JWT.JWTUtilities},
     * {@link org.springframework.security.core.userdetails.UserDetailsService}).
     */
    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
        return new AuthTokenFilter();
    }

    /**
     * BCrypt password encoder with default strength (10 rounds).
     * Used in {@link com.application.stockfela.service.UserService} for
     * hashing passwords on registration and verifying them on login.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * CORS configuration source – no wildcard origins.
     *
     * <p>Allowed origins are read from the {@code ALLOWED_ORIGINS} env var.
     * Credentials (cookies / Authorization header) are permitted because the
     * frontend sends a Bearer token.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Split on comma to support multiple origins from a single env var
        List<String> origins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();

        config.setAllowedOrigins(origins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Authorization", "Link", "X-Total-Count"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    /**
     * Main security filter chain.
     *
     * <p>Route rules:
     * <ul>
     *   <li>{@code /api/auth/**}  – public (register, login)</li>
     *   <li>{@code /h2-console/**} – public in dev; should be disabled in prod</li>
     *   <li>{@code /error}, {@code /favicon.ico} – public Spring Boot defaults</li>
     *   <li>Everything else – requires a valid JWT</li>
     * </ul>
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           CorsConfigurationSource corsConfigurationSource) throws Exception {
        http
                // CSRF not needed for stateless JWT REST APIs
                .csrf(AbstractHttpConfigurer::disable)

                // Apply CORS rules from the bean above
                .cors(cors -> cors.configurationSource(corsConfigurationSource))

                // Route-level access control
                .authorizeHttpRequests(authz -> authz
                        // Auth endpoints are always public
                        .requestMatchers("/api/auth/register", "/api/auth/login").permitAll()
                        // H2 console for local development only
                        .requestMatchers("/h2-console/**").permitAll()
                        // Spring Boot error and browser defaults
                        .requestMatchers("/error", "/favicon.ico").permitAll()
                        // Everything else must be authenticated
                        .anyRequest().authenticated()
                )

                // Allow H2 console to render inside iframes
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))

                // Stateless – no HTTP sessions
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Return 401 JSON instead of redirect to login page
                .exceptionHandling(ex ->
                        ex.authenticationEntryPoint(unauthorizedHandler))

                // JWT filter runs before the standard username/password filter
                .addFilterBefore(authenticationJwtTokenFilter(),
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Exposes the {@link AuthenticationManager} as a bean so controllers
     * can authenticate credentials programmatically (e.g., on login).
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration builder) throws Exception {
        return builder.getAuthenticationManager();
    }
}
