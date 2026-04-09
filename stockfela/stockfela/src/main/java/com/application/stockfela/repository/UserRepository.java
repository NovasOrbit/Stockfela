package com.application.stockfela.repository;

import com.application.stockfela.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for {@link User} entities.
 *
 * <p>Spring auto-generates the SQL for all method names following the
 * {@code findBy<Field>} / {@code existsBy<Field>} naming convention.
 * No manual implementation is required.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find a user by their exact username (case-sensitive).
     * Used by Spring Security during authentication and by controllers
     * resolving the current principal.
     *
     * @param username the username to search for
     * @return an {@link Optional} containing the user if found
     */
    Optional<User> findByUsername(String username);

    /**
     * Find a user by their e-mail address.
     *
     * @param email the e-mail address to search for (should be lower-cased)
     * @return an {@link Optional} containing the user if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Check whether a user with the given username already exists.
     * Used during registration to reject duplicate usernames.
     *
     * @param username the username to check
     * @return {@code true} if a user with this username exists
     */
    Boolean existsByUsername(String username);

    /**
     * Check whether a user with the given e-mail address already exists.
     * Used during registration to reject duplicate e-mails.
     *
     * @param email the e-mail address to check
     * @return {@code true} if a user with this email exists
     */
    Boolean existsByEmail(String email);
}
