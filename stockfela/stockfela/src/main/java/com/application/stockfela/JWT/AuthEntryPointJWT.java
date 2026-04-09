package com.application.stockfela.JWT;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Custom Spring Security authentication entry point.
 *
 * <p>Invoked whenever an unauthenticated request reaches a secured endpoint.
 * Instead of redirecting to a login page (the default HTML behaviour),
 * this returns a structured JSON 401 response — appropriate for a
 * stateless REST API consumed by a JavaScript frontend.
 *
 * <p>Registered in
 * {@link com.application.stockfela.config.SecurityConfig#filterChain}.
 */
@Component
public class AuthEntryPointJWT implements AuthenticationEntryPoint {

    private static final Logger logger = LoggerFactory.getLogger(AuthEntryPointJWT.class);

    /**
     * Writes a {@code 401 Unauthorized} JSON response.
     *
     * <p>Response body example:
     * <pre>{@code
     * {
     *   "status": 401,
     *   "error": "Unauthorized",
     *   "message": "Full authentication is required to access this resource",
     *   "path": "/api/groups"
     * }
     * }</pre>
     *
     * @param request       the request that triggered the authentication failure
     * @param response      the response to write the 401 body into
     * @param authException the exception carrying the authentication failure reason
     */
    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException)
            throws IOException, ServletException {

        logger.warn("Unauthorized request to '{}': {}",
                request.getServletPath(), authException.getMessage());

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        Map<String, Object> body = new HashMap<>();
        body.put("status", HttpServletResponse.SC_UNAUTHORIZED);
        body.put("error", "Unauthorized");
        body.put("message", authException.getMessage());
        body.put("path", request.getServletPath());

        new ObjectMapper().writeValue(response.getOutputStream(), body);
    }
}
