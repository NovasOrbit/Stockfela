package com.application.stockfela.config;

import com.application.stockfela.entity.Role;
import com.application.stockfela.entity.Role.RoleName;
import com.application.stockfela.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Database seeder that ensures the required {@link Role} rows exist
 * on every application startup.
 *
 * <p>Spring Security's role-based access control requires role records in the
 * database before any user can be registered. This seeder uses
 * {@code existsByName} checks before inserting, so it is safe to run
 * repeatedly (idempotent) and won't throw duplicate-key errors on restart.
 *
 * <p>Roles seeded:
 * <ul>
 *   <li>{@link RoleName#ROLE_USER}  – assigned to every new registrant by default.</li>
 *   <li>{@link RoleName#ROLE_ADMIN} – elevated privilege for administrators.</li>
 * </ul>
 */
@Configuration
public class RoleSeeder {

    /**
     * {@link CommandLineRunner} bean that seeds roles after the application
     * context is fully started.
     *
     * <p>Using a {@code CommandLineRunner} (rather than a static SQL script)
     * keeps the seeding logic in Java, which is easier to version, test, and
     * extend without managing dialect-specific SQL files.
     *
     * @param roleRepository JPA repository for role persistence
     * @return a {@link CommandLineRunner} that inserts missing roles
     */
    @Bean
    CommandLineRunner seedRoles(RoleRepository roleRepository) {
        return args -> {
            if (roleRepository.findByName(RoleName.ROLE_USER).isEmpty()) {
                roleRepository.save(new Role(RoleName.ROLE_USER));
            }
            if (roleRepository.findByName(RoleName.ROLE_ADMIN).isEmpty()) {
                roleRepository.save(new Role(RoleName.ROLE_ADMIN));
            }
        };
    }
}
