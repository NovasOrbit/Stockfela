package com.application.stockfela.config;

import org.springframework.context.annotation.Configuration;

/**
 * General Spring application configuration.
 *
 * <p>Currently a placeholder for application-wide beans that don't fit
 * in more specific config classes (e.g. {@link SecurityConfig}).
 *
 * <p><strong>Caching:</strong> {@code @EnableCaching} has been removed because
 * no cache implementation (Caffeine, Redis, etc.) is on the classpath.
 * Enabling the annotation without a cache manager silently falls back to a
 * no-op {@code ConcurrentMapCacheManager}, which wastes annotations and
 * misleads readers into thinking caching is active. Re-add when a real
 * cache dependency is included in {@code pom.xml}.
 */
@Configuration
public class AppConfig {
}
