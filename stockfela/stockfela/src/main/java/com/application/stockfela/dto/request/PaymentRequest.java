package com.application.stockfela.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Request body for recording a member's payment within a payout cycle.
 *
 * <pre>POST /api/payouts/{payoutCycleId}/pay</pre>
 *
 * <p>Example JSON:
 * <pre>{@code { "userId": 2, "amount": 1000.00 }}</pre>
 */
@Data
public class PaymentRequest {

    /** Database ID of the user making the payment. */
    @NotNull(message = "User ID is required")
    private Long userId;

    /**
     * The amount being paid.
     * Should match the group's fixed {@code monthlyContribution}, but the
     * actual amount is recorded for auditing purposes.
     */
    @NotNull(message = "Payment amount is required")
    @DecimalMin(value = "0.01", message = "Payment amount must be greater than zero")
    private BigDecimal amount;
}
