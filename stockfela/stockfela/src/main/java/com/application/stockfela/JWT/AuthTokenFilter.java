package com.application.stockfela.JWT;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Spring Security filter that validates the JWT on every incoming request.
 *
 * <p>Extends {@link OncePerRequestFilter} to guarantee it runs exactly once
 * per request, even if the filter chain is re-entered (e.g. via a forward).
 *
 * <p>Processing logic:
 * <ol>
 *   <li>Extract the JWT from the {@code Authorization: Bearer <token>} header.</li>
 *   <li>Validate the token's signature and expiry via {@link JWTUtilities}.</li>
 *   <li>Load the {@link UserDetails} for the token's subject (username).</li>
 *   <li>Build a {@link UsernamePasswordAuthenticationToken} and store it in the
 *       {@link SecurityContextHolder} so downstream code knows who made the request.</li>
 * </ol>
 *
 * <p>If no token is present, or the token is invalid, the filter simply
 * passes the request on without setting any authentication — the downstream
 * {@link com.application.stockfela.config.SecurityConfig} route rules then
 * decide whether the unauthenticated request should be allowed or rejected.
 *
 * <p>Registered in
 * {@link com.application.stockfela.config.SecurityConfig#filterChain} before
 * {@link org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter}.
 */
@RequiredArgsConstructor
public class AuthTokenFilter extends OncePerRequestFilter {

    /** Utility class for extracting, generating, and validating JWTs. */
    private final JWTUtilities jwtUtilities;

    /**
     * Loads full user details (including authorities) from the database
     * so they can be attached to the authentication object.
     */
    private final UserDetailsService userDetailsService;

    private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);

    /**
     * Core filter method: validates the JWT and sets the security context.
     *
     * @param request     the incoming HTTP request
     * @param response    the HTTP response (not modified here)
     * @param filterChain the remaining filters to invoke after this one
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        logger.debug("AuthTokenFilter invoked for: {}", request.getRequestURI());

        try {
            String jwt = jwtUtilities.getJwtForHeader(request);

            if (jwt != null && jwtUtilities.validateJwtToken(jwt)) {
                String username = jwtUtilities.getUserNameFromJwtToken(jwt);
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());

                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);

                logger.debug("Authenticated user '{}' with roles: {}",
                        username, userDetails.getAuthorities());
            }
        } catch (Exception e) {
            logger.error("Cannot set user authentication: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
