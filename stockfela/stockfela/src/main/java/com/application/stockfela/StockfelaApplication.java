package com.application.stockfela;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Stockfela Spring Boot application.
 *
 * <p><strong>What is Stockfela?</strong><br>
 * Stockfela is a digital ROSCA (Rotating Savings and Credit Association)
 * platform — known as a "stokvel" in South African culture. Members pool
 * fixed monthly contributions and the full pot rotates to one member per
 * month until everyone has received it.
 *
 * <p><strong>Architecture overview:</strong>
 * <pre>
 * HTTP Request
 *     │
 *     ▼
 * AuthTokenFilter  (validates JWT, sets SecurityContext)
 *     │
 *     ▼
 * Controller layer (/api/auth, /api/groups, /api/payouts)
 *     │
 *     ▼
 * Service layer    (UserService, SavingGroupService, PayoutCycleService)
 *     │
 *     ▼
 * Repository layer (Spring Data JPA)
 *     │
 *     ▼
 * Database         (H2 in-memory for dev / MySQL for production)
 * </pre>
 *
 * <p>See {@code README.md} at the project root for setup instructions and
 * the full API reference.
 */
@SpringBootApplication
public class StockfelaApplication {

    /**
     * Application entry point.
     *
     * @param args command-line arguments passed to the JVM
     */
    public static void main(String[] args) {
        SpringApplication.run(StockfelaApplication.class, args);
    }
}
