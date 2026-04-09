package com.application.stockfela.dto.response;

import lombok.Builder;
import lombok.Data;

/**
 * Embedded status object included in API response DTOs.
 *
 * <p>Provides a machine-readable {@link #code} (mirrors HTTP status codes)
 * and a human-readable {@link #message} for the frontend to display.
 *
 * <p>Instances are built by
 * {@link com.application.stockfela.dto.StockfelaMapper} using constants
 * from {@link com.application.stockfela.dto.ProcessStatusCodes}.
 *
 * <p>Example JSON:
 * <pre>{@code { "code": "200", "message": "Success" }}</pre>
 */
@Data
@Builder
public class ProcessStatus {

    /** HTTP-style status code as a string, e.g. {@code "200"} or {@code "409"}. */
    private String code;

    /** Short human-readable description, e.g. {@code "Success"}. */
    private String message;
}
