package com.application.stockfela.repository;

import com.application.stockfela.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for {@link Role} entities.
 *
 * <p>Role records are seeded on startup by
 * {@link com.application.stockfela.config.RoleSeeder} and are only
 * ever read at runtime (never updated or deleted), so only a lookup
 * method is needed beyond the standard {@link JpaRepository} CRUD.
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * Find a role by its enum name.
     * Used by the seeder (to check for duplicates before inserting) and by
     * {@link com.application.stockfela.service.UserService} during registration
     * to resolve role name strings to entity references.
     *
     * @param name the {@link Role.RoleName} enum constant to search for
     * @return an {@link Optional} containing the role if it has been seeded
     */
    Optional<Role> findByName(Role.RoleName name);
}
