package com.application.stockfela.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * JPA entity representing a registered user of the Stockfela platform.
 *
 * <p>A user can:
 * <ul>
 *   <li><strong>Create</strong> savings groups (becomes the first member).</li>
 *   <li><strong>Join</strong> existing groups as a member.</li>
 *   <li><strong>Contribute</strong> monthly payments toward their group's pool.</li>
 *   <li><strong>Receive</strong> the pooled payout when it is their turn.</li>
 * </ul>
 *
 * <p>Passwords are stored BCrypt-hashed – never in plain text.
 * The raw password is never returned in API responses (controllers and
 * mappers should exclude it).
 *
 * <p>Mapped to the {@code users} table.
 */
@Entity
@Table(name = "users")
public class User {

    // ── Primary key ──────────────────────────────────────────────────────────

    /** Auto-generated database primary key. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── Core fields ──────────────────────────────────────────────────────────

    /** Unique login handle chosen at registration (max 50 characters). */
    @Column(unique = true, nullable = false, length = 50)
    @NotBlank(message = "Username is required")
    private String username;

    /** Unique e-mail address; stored lower-cased. */
    @Column(unique = true, nullable = false, length = 100)
    @Email(message = "Email should be valid")
    @NotBlank(message = "Email is required")
    private String email;

    /** BCrypt-hashed password. Never expose in responses. */
    @Column(nullable = false)
    @NotBlank(message = "Password is required")
    private String password;

    /** User's display name shown across the application. */
    @Column(name = "full_name", nullable = false, length = 100)
    @NotBlank(message = "Full name is required")
    private String fullName;

    /** Optional contact phone number (no format constraint enforced at DB level). */
    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    // ── Status ───────────────────────────────────────────────────────────────

    /**
     * Whether this account is active.
     * Soft-delete flag: set to {@code false} instead of deleting rows.
     * Maps to the {@code enabled} column.
     */
    @Column(name = "enabled")
    private boolean isActive = true;

    // ── Relationships ────────────────────────────────────────────────────────

    /**
     * All savings groups this user belongs to (either as creator or added member).
     * Managed via the {@link GroupMember} join entity.
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<GroupMember> groupMemberships;

    /**
     * All savings groups this user has created.
     * A user remains a member of a group they created even after transferring
     * administration (future feature).
     */
    @OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL)
    private List<SavingsGroup> createdGroups;

    /**
     * Security roles assigned to this user (e.g. ROLE_USER, ROLE_ADMIN).
     * Fetched eagerly so Spring Security can build the authority list
     * without a separate query on every request.
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    // ── Audit timestamps ─────────────────────────────────────────────────────

    /** Timestamp automatically set when the row is first inserted. */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /** Timestamp automatically updated on every modification. */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ── Constructors ─────────────────────────────────────────────────────────

    /** Required by JPA. */
    public User() {}

    /**
     * Convenience constructor used in tests and seeders.
     *
     * @param username login handle
     * @param email    e-mail address
     * @param password plain-text password (encode before calling this in production)
     * @param fullName display name
     */
    public User(String username, String email, String password, String fullName) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.fullName = fullName;
    }

    // ── Getters and Setters ──────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public Set<Role> getRoles() { return roles; }
    public void setRoles(Set<Role> roles) { this.roles = roles; }

    public List<GroupMember> getGroupMemberships() { return groupMemberships; }
    public void setGroupMemberships(List<GroupMember> groupMemberships) {
        this.groupMemberships = groupMemberships;
    }

    public List<SavingsGroup> getCreatedGroups() { return createdGroups; }
    public void setCreatedGroups(List<SavingsGroup> createdGroups) {
        this.createdGroups = createdGroups;
    }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
