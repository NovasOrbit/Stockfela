package com.application.stockfela.globalException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Centralised exception handler for all REST controllers.
 *
 * <p>{@link RestControllerAdvice} intercepts exceptions thrown anywhere in the
 * controller layer (and their service delegates) and converts them into
 * consistent, structured JSON error responses — preventing raw stack traces
 * or Spring's default HTML error pages from leaking to API consumers.
 *
 * <p>Every error response follows this shape:
 * <pre>{@code
 * {
 *   "timestamp": "2026-04-09T10:15:30Z",
 *   "status":    400,
 *   "error":     "Bad Request",
 *   "message":   "Username already exists",
 *   "path":      "/api/auth/register"
 * }
 * }</pre>
 *
 * <p>Handlers are ordered from most-specific to least-specific so the most
 * precise handler matches first.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ── 400 Bad Request ──────────────────────────────────────────────────────

    /**
     * Handles Bean Validation failures (triggered by {@code @Valid}).
     * Returns a map of {@code fieldName → errorMessage} for each invalid field.
     *
     * @param ex the validation exception populated by Spring MVC
     * @return 400 response with per-field error details
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
            MethodArgumentNotValidException ex, WebRequest request) {

        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(err -> fieldErrors.put(err.getField(), err.getDefaultMessage()));

        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Validation failed",
                fieldErrors.toString(), request);
    }

    /**
     * Handles programmatic argument validation (thrown by service layer).
     *
     * @param ex      the exception thrown with a descriptive message
     * @param request the current web request (for path info)
     * @return 400 response
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(
            IllegalArgumentException ex, WebRequest request) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Bad Request",
                ex.getMessage(), request);
    }

    /**
     * Handles bad login credentials from Spring Security.
     *
     * @param ex      the authentication exception
     * @param request the current web request
     * @return 401 response (credentials wrong, not authorisation failure)
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentials(
            BadCredentialsException ex, WebRequest request) {
        // Use a generic message — never reveal whether username or password was wrong
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, "Unauthorized",
                "Invalid username or password", request);
    }

    // ── 403 Forbidden ────────────────────────────────────────────────────────

    /**
     * Handles access-denied exceptions thrown by Spring Security
     * (e.g. when a user lacks the required role for a method
     * guarded by {@code @PreAuthorize}).
     *
     * @param ex      the access-denied exception
     * @param request the current web request
     * @return 403 response
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(
            AccessDeniedException ex, WebRequest request) {
        return buildErrorResponse(HttpStatus.FORBIDDEN, "Forbidden",
                "You do not have permission to perform this action", request);
    }

    // ── 404 Not Found ────────────────────────────────────────────────────────

    /**
     * Handles business-logic "not found" cases (e.g. group not found, user not found).
     * Services throw {@link RuntimeException} with descriptive messages; this
     * handler catches them and promotes to a proper 404.
     *
     * <p>Note: narrow this handler over time by creating a dedicated
     * {@code ResourceNotFoundException extends RuntimeException} and catching
     * that instead of the broad RuntimeException.
     *
     * @param ex      the runtime exception from the service layer
     * @param request the current web request
     * @return 404 response when the message hints at a "not found" scenario,
     *         or 400 for other runtime errors
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(
            RuntimeException ex, WebRequest request) {

        String message = ex.getMessage();
        boolean isNotFound = message != null && (
                message.toLowerCase().contains("not found") ||
                message.toLowerCase().contains("does not exist"));

        if (isNotFound) {
            logger.warn("Resource not found: {}", message);
            return buildErrorResponse(HttpStatus.NOT_FOUND, "Not Found", message, request);
        }

        boolean isConflict = message != null && (
                message.toLowerCase().contains("already exists") ||
                message.toLowerCase().contains("already a member") ||
                message.toLowerCase().contains("duplicate"));

        if (isConflict) {
            logger.warn("Conflict: {}", message);
            return buildErrorResponse(HttpStatus.CONFLICT, "Conflict", message, request);
        }

        logger.error("Unhandled runtime exception", ex);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error", "An unexpected error occurred", request);
    }

    // ── Fallback ─────────────────────────────────────────────────────────────

    /**
     * Catch-all handler for any exception not matched above.
     * Logs the full stack trace internally but returns a generic message
     * to avoid leaking implementation details.
     *
     * @param ex      the unhandled exception
     * @param request the current web request
     * @return 500 response with a generic message
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(
            Exception ex, WebRequest request) {
        logger.error("Unexpected exception", ex);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error", "An unexpected error occurred", request);
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    /**
     * Constructs a consistent error response body.
     *
     * @param status  the HTTP status to return
     * @param error   short error label (e.g. "Bad Request")
     * @param message detailed error message
     * @param request the web request (used to extract the request path)
     * @return a {@link ResponseEntity} with the standard error body
     */
    private ResponseEntity<Map<String, Object>> buildErrorResponse(
            HttpStatus status, String error, String message, WebRequest request) {

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", status.value());
        body.put("error", error);
        body.put("message", message);
        body.put("path", request.getDescription(false).replace("uri=", ""));

        return ResponseEntity.status(status).body(body);
    }
}
