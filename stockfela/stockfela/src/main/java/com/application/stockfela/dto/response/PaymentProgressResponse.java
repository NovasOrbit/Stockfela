package com.application.stockfela.dto.response;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Read-only response DTO representing the payment collection progress
 * for a single payout cycle.
 *
 * <p>Returned by {@code GET /api/payouts/{payoutCycleId}/progress}.
 *
 * <p>DTOs belong in the {@code dto.response} package, not inside a
 * service class. Keeping them separate:
 * <ul>
 *   <li>Makes the package layout predictable.</li>
 *   <li>Allows controllers and tests to reference the type without
 *       importing the service.</li>
 *   <li>Follows the Single-Responsibility Principle.</li>
 * </ul>
 */
public class PaymentProgressResponse {

    /** Total number of members in the group (= number of contributions expected). */
    private final long totalMembers;

    /** Number of members who have already paid this cycle. */
    private final long paidMembers;

    /** Expected total amount to be collected (members × monthly contribution). */
    private final BigDecimal totalExpected;

    /** Amount actually collected so far (sum of PAID contributions). */
    private final BigDecimal totalCollected;

    // ── Constructor ──────────────────────────────────────────────────────────

    /**
     * @param totalMembers   total members in the group
     * @param paidMembers    members that have already paid
     * @param totalExpected  total amount expected for this cycle
     * @param totalCollected amount collected so far
     */
    public PaymentProgressResponse(long totalMembers, long paidMembers,
                                   BigDecimal totalExpected, BigDecimal totalCollected) {
        this.totalMembers    = totalMembers;
        this.paidMembers     = paidMembers;
        this.totalExpected   = totalExpected;
        this.totalCollected  = totalCollected;
    }

    // ── Accessors ────────────────────────────────────────────────────────────

    /** @return total members expected to contribute */
    public long getTotalMembers() { return totalMembers; }

    /** @return members who have already paid */
    public long getPaidMembers() { return paidMembers; }

    /** @return expected total collection amount */
    public BigDecimal getTotalExpected() { return totalExpected; }

    /** @return amount collected so far */
    public BigDecimal getTotalCollected() { return totalCollected; }

    /**
     * Calculates what percentage of members have paid.
     *
     * @return a value in the range {@code [0.0, 100.0]},
     *         or {@code 0.0} if there are no members
     */
    public double getPaymentPercentage() {
        if (totalMembers == 0) return 0.0;
        return BigDecimal.valueOf(paidMembers)
                .divide(BigDecimal.valueOf(totalMembers), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
    }
}
