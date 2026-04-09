package com.application.stockfela.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Set;

/**
 * Request body for the user registration endpoint
 * {@code POST /api/auth/register}.
 *
 * <p>All fields are validated by Bean Validation before the controller
 * method executes (triggered by {@code @Valid} on the parameter).
 * Jackson deserialises the JSON body into this DTO.
 *
 * <p>Why {@code @NoArgsConstructor} + {@code @AllArgsConstructor}?
 * {@code @Builder} generates a package-private all-args constructor, but
 * Jackson's default object mapper requires a no-args constructor to
 * deserialise JSON. Adding both avoids a
 * {@code InvalidDefinitionException} at runtime.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    /** The unique login handle the user wants (3–50 characters). */
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    /** Valid e-mail address; stored in lower-case. */
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    /** Plain-text password (will be BCrypt-hashed before storage). */
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    /** User's display name shown across the application. */
    @NotBlank(message = "Full name is required")
    @JsonProperty("fullName")
    private String fullName;

    /** Optional contact number (no format validation enforced here). */
    private String phoneNumber;

    /**
     * Set of role names to assign (e.g. {@code "ROLE_USER"}).
     * Field name follows camelCase Java convention.
     * The service layer converts these strings to {@link com.application.stockfela.entity.Role.RoleName} enums.
     */
    private Set<String> roles;
}
