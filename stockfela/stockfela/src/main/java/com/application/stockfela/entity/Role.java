package com.application.stockfela.entity;

import jakarta.persistence.*;

/**
 * JPA entity representing a security role that can be assigned to a {@link User}.
 *
 * <p>Roles control what a user is authorised to do. Currently two roles exist:
 * <ul>
 *   <li>{@link RoleName#ROLE_USER}  – standard registered user.</li>
 *   <li>{@link RoleName#ROLE_ADMIN} – platform administrator.</li>
 * </ul>
 *
 * <p>Roles are pre-seeded by {@link com.application.stockfela.config.RoleSeeder}
 * on application startup. They are assigned to users during registration via
 * the {@code user_roles} join table.
 *
 * <p>Mapped to the {@code roles} table.
 */
@Entity
@Table(name = "roles")
public class Role {

    // ── Primary key ──────────────────────────────────────────────────────────

    /** Auto-generated database primary key. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── Core field ───────────────────────────────────────────────────────────

    /**
     * The role name enum value, stored as a string in the database.
     * The column is unique so each role exists only once.
     */
    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false, unique = true)
    private RoleName name;

    // ── Enum ─────────────────────────────────────────────────────────────────

    /**
     * Enumeration of available role names.
     *
     * <p>Prefixed with {@code ROLE_} as required by Spring Security's
     * {@code hasRole()} / {@code hasAuthority()} mechanism.
     */
    public enum RoleName {
        /** Standard user – can create/join groups and make contributions. */
        ROLE_USER,
        /** Administrator – has elevated privileges across the platform. */
        ROLE_ADMIN
    }

    // ── Constructors ─────────────────────────────────────────────────────────

    /** Required by JPA. */
    public Role() {}

    /**
     * Convenience constructor used in {@link com.application.stockfela.config.RoleSeeder}.
     *
     * @param name the role to assign
     */
    public Role(RoleName name) {
        this.name = name;
    }

    // ── Getters and Setters ──────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public RoleName getName() { return name; }
    public void setName(RoleName name) { this.name = name; }
}
