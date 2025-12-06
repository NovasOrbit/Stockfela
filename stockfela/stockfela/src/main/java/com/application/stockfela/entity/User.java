package com.application.stockfela.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // This annotation maps to the 'username' column in our users table
    @Column(unique = true, nullable = false, length = 50)
    @NotBlank(message = "Username is required")
    private String username;

    @Column(unique = true, nullable = false, length = 100)
    @Email(message = "Email should be valid")
    @NotBlank(message = "Email is required")
    private String email;

    @Column(nullable = false)
    @NotBlank(message = "Password is required")
    private String password;

    @Column(name = "full_name", nullable = false, length = 100)
    @NotBlank(message = "Full name is required")
    private String fullName;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    // One user can be in multiple groups
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<GroupMember> groupMemberships;

    // One user can create multiple groups
    @OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL)
    private List<SavingsGroup> createdGroups;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors
    public User() {}

    public User(String username, String email, String password, String fullName) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.fullName = fullName;
    }

    // Getters and Setters (VERY IMPORTANT in JPA)
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

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<GroupMember> getGroupMemberships() { return groupMemberships; }
    public void setGroupMemberships(List<GroupMember> groupMemberships) { this.groupMemberships = groupMemberships; }

    public List<SavingsGroup> getCreatedGroups() { return createdGroups; }
    public void setCreatedGroups(List<SavingsGroup> createdGroups) { this.createdGroups = createdGroups; }
}
