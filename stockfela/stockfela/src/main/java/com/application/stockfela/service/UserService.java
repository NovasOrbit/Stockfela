package com.application.stockfela.service;

import com.application.stockfela.dto.request.RegisterRequest;
import com.application.stockfela.dto.response.RegisterResponse;
import com.application.stockfela.entity.Role;
import com.application.stockfela.entity.User;
import com.application.stockfela.repository.RoleRepository;
import com.application.stockfela.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.application.stockfela.dto.StockfelaMapper.mapToRegisterResponse;

/**
 * Service layer for user account management.
 *
 * <p>Implements {@link UserDetailsService} so Spring Security can load user
 * details from the database when validating a JWT or authenticating a login.
 *
 * <p>All dependencies are injected via constructor thanks to
 * {@link RequiredArgsConstructor} — never mix {@code @Autowired} with
 * {@code final} fields ({@code @RequiredArgsConstructor} already generates
 * the required constructor).
 */
@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    /** JPA repository for CRUD operations on {@link User} entities. */
    private final UserRepository userRepository;

    /** BCrypt encoder injected from {@link com.application.stockfela.config.SecurityConfig}. */
    private final PasswordEncoder passwordEncoder;

    /** Repository used to look up {@link Role} entities by name during registration. */
    private final RoleRepository roleRepository;

    // ── Registration ────────────────────────────────────────────────────────

    /**
     * Register a new user account.
     *
     * <p>Steps:
     * <ol>
     *   <li>Reject duplicate username or e-mail.</li>
     *   <li>Map DTO fields onto a new {@link User} entity.</li>
     *   <li>BCrypt-hash the password.</li>
     *   <li>Resolve role names to {@link Role} entities (must be pre-seeded
     *       by {@link com.application.stockfela.config.RoleSeeder}).</li>
     *   <li>Persist and return the mapped response DTO.</li>
     * </ol>
     *
     * @param registerRequest validated request from the controller
     * @return a {@link RegisterResponse} containing the saved user's public fields
     * @throws RuntimeException if the username or email is already taken
     * @throws IllegalStateException if a requested role hasn't been seeded yet
     */
    public RegisterResponse registerUser(RegisterRequest registerRequest) {

        // 1) Reject duplicates
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // 2) Map request DTO → entity
        User user = new User();
        user.setUsername(registerRequest.getUsername().trim());
        user.setEmail(registerRequest.getEmail().trim().toLowerCase());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setFullName(registerRequest.getFullName().trim());
        user.setPhoneNumber(registerRequest.getPhoneNumber());

        // 3) Assign roles; default to ROLE_USER when the request omits roles
        Set<String> requestedRoles = (registerRequest.getRoles() == null || registerRequest.getRoles().isEmpty())
                ? Set.of(Role.RoleName.ROLE_USER.name())
                : registerRequest.getRoles();

        Set<Role> roles = requestedRoles.stream()
                .map(Role.RoleName::valueOf)
                .map(roleName -> roleRepository.findByName(roleName)
                        .orElseThrow(() -> new IllegalStateException("Role not seeded: " + roleName)))
                .collect(Collectors.toSet());

        user.setRoles(roles);

        // 4) Persist
        User saved = userRepository.save(user);

        return mapToRegisterResponse(saved);
    }

    // ── Lookup helpers ──────────────────────────────────────────────────────

    /**
     * Find a user by username.
     *
     * @param username the exact username to look up
     * @return an {@link Optional} containing the user if found
     */
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Find a user by their database primary key.
     *
     * @param id the user's ID
     * @return an {@link Optional} containing the user if found
     */
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * Persist changes to an existing user (e.g. profile update).
     *
     * @param user the user entity with updated fields
     * @return the saved entity (may differ if auditing fields were updated)
     */
    public User updateUser(User user) {
        return userRepository.save(user);
    }

    /**
     * Returns all users — intended for internal/admin use only.
     * Callers should ensure this is only accessible to admins.
     *
     * @return list of all registered users
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // ── Spring Security integration ─────────────────────────────────────────

    /**
     * Loads a user by username for Spring Security authentication.
     *
     * <p>Called by the authentication manager during login and by the JWT
     * filter on every authenticated request.
     *
     * @param username the username extracted from the login request or JWT
     * @return a {@link UserDetails} object with authorities derived from roles
     * @throws UsernameNotFoundException if no user exists with that username
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with username: " + username));

        List<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName().name()))
                .toList();

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                authorities);
    }
}
