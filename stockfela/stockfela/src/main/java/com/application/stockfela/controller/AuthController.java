package com.application.stockfela.controller;

import com.application.stockfela.JWT.JWTUtilities;
import com.application.stockfela.dto.request.LoginRequest;
import com.application.stockfela.dto.request.RegisterRequest;
import com.application.stockfela.dto.response.LoginResponse;
import com.application.stockfela.dto.response.RegisterResponse;
import com.application.stockfela.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST controller handling user authentication: registration and login.
 *
 * <p>Endpoints under {@code /api/auth} are explicitly whitelisted in
 * {@link com.application.stockfela.config.SecurityConfig} so they can be
 * accessed without a JWT token.
 *
 * <p>Constructor injection (via {@code @RequiredArgsConstructor}) is used
 * instead of field injection for testability and to make dependencies explicit.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    /** Service layer for user registration, lookup, and Spring UserDetails. */
    private final UserService userService;

    /** Spring Security authentication manager – validates credentials. */
    private final AuthenticationManager authenticationManager;

    /** Utility for generating and validating JWT tokens. */
    private final JWTUtilities jwtUtilities;

    // ── Endpoints ───────────────────────────────────────────────────────────

    /**
     * Register a new user account.
     *
     * <pre>POST /api/auth/register</pre>
     *
     * @param registerRequest validated registration payload
     * @return 201 Created with user details, or 400/409 on validation failure
     */
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> registerUser(
            @Valid @RequestBody RegisterRequest registerRequest) {
        RegisterResponse response = userService.registerUser(registerRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Authenticate an existing user and return a JWT access token.
     *
     * <pre>POST /api/auth/login</pre>
     *
     * @param loginRequest validated login credentials
     * @return 200 OK with JWT token and role list, or 400 on bad credentials
     */
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);



            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            List<String> roles = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());

            String jwtToken = jwtUtilities.generateTokenFromUsername(userDetails,roles);



            return ResponseEntity.ok(new LoginResponse(jwtToken, userDetails.getUsername(), roles));

        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}
