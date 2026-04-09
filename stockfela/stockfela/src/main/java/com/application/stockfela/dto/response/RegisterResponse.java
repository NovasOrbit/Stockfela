package com.application.stockfela.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

/**
 * Response DTO returned after a successful user registration.
 *
 * <p>Returned by {@code POST /api/auth/register} with HTTP status 201.
 *
 * <p>Intentionally excludes the password field — the password hash must
 * never leave the server.
 *
 * <p>Example JSON:
 * <pre>{@code
 * {
 *   "status": { "code": "200", "message": "Success" },
 *   "id": 1,
 *   "username": "john_doe",
 *   "email": "john@example.com",
 *   "fullName": "John Doe",
 *   "role": ["ROLE_USER"]
 * }
 * }</pre>
 */
@Data
@Builder
public class RegisterResponse {

    /** Embedded process status indicating success or a specific error code. */
    private ProcessStatus status;

    /** The newly assigned database primary key for the user. */
    private Long id;

    /** The user's chosen login handle. */
    private String username;

    /** The user's e-mail address (stored lower-cased). */
    private String email;

    /** The user's display name. */
    private String fullName;

    /** Set of role name strings (e.g. {@code "ROLE_USER"}) assigned to the user. */
    private Set<String> role;
}
