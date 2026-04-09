package com.application.stockfela.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Response DTO returned after a successful login.
 *
 * <p>Returned by {@code POST /api/auth/login} with HTTP status 200.
 *
 * <p>The frontend should store the {@link #jwtToken} and send it as
 * {@code Authorization: Bearer <token>} on every subsequent request.
 *
 * <p>Example JSON:
 * <pre>{@code
 * {
 *   "jwtToken": "eyJhbGciOiJIUzI1NiJ9...",
 *   "username": "john_doe",
 *   "roles": ["ROLE_USER"]
 * }
 * }</pre>
 */
@Data
@Builder
@AllArgsConstructor
public class LoginResponse {

    /** Optional user ID – not currently populated but reserved for future use. */
    private String id;

    /** The authenticated user's login handle. */
    private String username;

    /** The authenticated user's e-mail address. */
    private String email;

    /** The authenticated user's display name. */
    private String fullName;

    /**
     * Signed JWT access token.
     * Include in subsequent requests as: {@code Authorization: Bearer <jwtToken>}
     */
    private String jwtToken;

    /** List of authority strings derived from the user's roles (e.g. {@code "ROLE_USER"}). */
    private List<String> roles;

    /**
     * Convenience constructor used by {@link com.application.stockfela.controller.AuthController}
     * when only the token, username, and roles are available from the authentication result.
     *
     * @param jwtToken the signed JWT
     * @param username the authenticated username
     * @param roles    list of role authority strings
     */
    public LoginResponse(String jwtToken, String username, List<String> roles) {
        this.jwtToken = jwtToken;
        this.username = username;
        this.roles = roles;
    }
}
