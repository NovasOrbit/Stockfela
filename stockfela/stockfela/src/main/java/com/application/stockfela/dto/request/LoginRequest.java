package com.application.stockfela.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request body for the login endpoint {@code POST /api/auth/login}.
 *
 * <p>Bean Validation constraints are applied here so that the controller
 * can rely on {@code @Valid} to reject blank credentials before attempting
 * authentication, producing a clear 400 response instead of a cryptic
 * authentication failure.
 */
@Data
public class LoginRequest {

    /** The user's registered username. Must not be blank. */
    @NotBlank(message = "Username is required")
    private String username;

    /** The user's plain-text password. Must not be blank. */
    @NotBlank(message = "Password is required")
    private String password;
}
