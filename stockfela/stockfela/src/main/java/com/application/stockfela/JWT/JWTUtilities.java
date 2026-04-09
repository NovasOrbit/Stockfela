package com.application.stockfela.JWT;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * Utility class for JSON Web Token (JWT) operations.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Extract the JWT from the HTTP {@code Authorization} header.</li>
 *   <li>Generate a signed JWT for a given {@link UserDetails}.</li>
 *   <li>Parse and validate an incoming JWT.</li>
 * </ul>
 *
 * <p><strong>Key derivation:</strong> The secret stored in {@code JWT_SECRET}
 * must be a Base64-encoded string of at least 32 bytes (256 bits) for HS256.
 * Generate one with: {@code openssl rand -base64 64}.
 * The secret is decoded from Base64 before creating the HMAC key, which is the
 * correct approach — using raw string bytes is insecure when the string contains
 * only ASCII characters (far less entropy than 256 random bits).
 */
@Component
public class JWTUtilities {

    private static final Logger logger = LoggerFactory.getLogger(JWTUtilities.class);

    /**
     * Base64-encoded HMAC-SHA256 signing secret.
     * Set via the {@code JWT_SECRET} environment variable (see {@code .env.example}).
     */
    @Value("${jwt.secret}")
    private String jwtSecret;

    /**
     * Token validity window in milliseconds.
     * Defaults to 86 400 000 ms (24 hours). Override via {@code JWT_EXPIRATION_MS}.
     */
    @Value("${jwt.expiration}")
    private long jwtExpirationMs;

    // ── Public API ──────────────────────────────────────────────────────────

    /**
     * Extracts the raw JWT string from the {@code Authorization: Bearer <token>}
     * request header.
     *
     * @param request the incoming HTTP request
     * @return the JWT string, or {@code null} if the header is absent / malformed
     */
    public String getJwtForHeader(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        logger.debug("Authorization Header present: {}", bearerToken != null);

        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * Generates a signed JWT for the given user.
     *
     * <p>The token encodes the username as the subject claim and is signed
     * with HS256. Expiry is set to {@code now + jwtExpirationMs}.
     *
     * @param userDetails the authenticated user (username is used as subject)
     * @return a compact, URL-safe JWT string
     */
    public String generateTokenFromUsername(UserDetails userDetails) {
        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(signingKey())
                .compact();
    }

    /**
     * Extracts the username (subject claim) from a validated JWT.
     *
     * @param token a valid, non-expired JWT string
     * @return the username embedded in the token's subject claim
     */
    public String getUserNameFromJwtToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    /**
     * Validates a JWT token: checks signature, expiry, and format.
     *
     * @param authToken the JWT string to validate
     * @return {@code true} if the token is valid; {@code false} otherwise
     */
    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(signingKey())
                    .build()
                    .parseClaimsJws(authToken);
            return true;
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    // ── Private helpers ─────────────────────────────────────────────────────

    /**
     * Derives the HMAC-SHA256 signing key from the Base64-encoded secret.
     *
     * <p>Using {@link Decoders#BASE64} ensures the key has the full entropy
     * of the original random bytes, rather than the reduced entropy of ASCII
     * string bytes. The secret must decode to ≥ 32 bytes for HS256.
     *
     * @return a {@link SecretKey} suitable for HS256 signing/verification
     */
    private SecretKey signingKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
