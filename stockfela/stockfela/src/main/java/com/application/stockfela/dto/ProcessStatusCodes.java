package com.application.stockfela.dto;

/**
 * Enumeration of application-level process status codes.
 *
 * <p>Each constant maps a human-readable name to an HTTP-style numeric code
 * and a short descriptive message. These are used by
 * {@link com.application.stockfela.dto.StockfelaMapper} to build
 * {@link com.application.stockfela.dto.response.ProcessStatus} objects
 * that are embedded in API responses.
 *
 * <p>Naming convention: {@code <DOMAIN>_<CONDITION>}, e.g.
 * {@code REGISTRATION_ALREADY_EXISTS}.
 */
public enum ProcessStatusCodes {

    /** Generic success – the operation completed without errors. */
    SUCCESS("200", "Success"),

    /** The registration request was malformed or failed validation. */
    REGISTRATION_BAD_REQUEST("400", "Registration Bad Request"),

    /** A user with the same username or email already exists. */
    REGISTRATION_ALREADY_EXISTS("409", "Already exists"),

    /** Login request contained invalid or missing fields. */
    LOGIN_INPUT_VALIDATION_ERROR("400", "Login input data error");

    // ── Fields ──────────────────────────────────────────────────────────────

    /** HTTP-style status code as a string (e.g. {@code "200"}). */
    private final String code;

    /** Short human-readable description of this status. */
    private final String message;

    // ── Constructor ─────────────────────────────────────────────────────────

    ProcessStatusCodes(String code, String message) {
        this.code = code;
        this.message = message;
    }

    // ── Accessors ───────────────────────────────────────────────────────────

    /** Returns the HTTP-style numeric code string. */
    public String getCode() {
        return code;
    }

    /** Returns the short status message. */
    public String getMessage() {
        return message;
    }
}
