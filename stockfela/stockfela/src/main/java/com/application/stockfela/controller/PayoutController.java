package com.application.stockfela.controller;

import com.application.stockfela.dto.request.PaymentRequest;
import com.application.stockfela.dto.response.PaymentProgressResponse;
import com.application.stockfela.service.PayoutCycleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for payout-cycle and contribution operations.
 *
 * <p>All endpoints require an authenticated user (JWT). They allow
 * members to record their payments and query collection progress
 * for a given payout cycle.
 */
@RestController
@RequestMapping("/api/payouts")
@RequiredArgsConstructor
public class PayoutController {

    /** Service handling payout-cycle lifecycle and contribution recording. */
    private final PayoutCycleService payoutCycleService;

    // ── Write ────────────────────────────────────────────────────────────────

    /**
     * Record a member's payment for a payout cycle.
     *
     * <pre>POST /api/payouts/{payoutCycleId}/pay</pre>
     *
     * Example request body:
     * <pre>{@code { "userId": 2, "amount": 1000.00 }}</pre>
     *
     * <p>After recording, if all members have paid the cycle status is
     * automatically changed to {@code COMPLETED}.
     *
     * @param payoutCycleId the active payout cycle's ID
     * @param request       user ID and payment amount
     * @return the updated contribution record, or 400 on error
     */
    @PostMapping("/{payoutCycleId}/pay")
    public ResponseEntity<?> recordPayment(
            @PathVariable Long payoutCycleId,
            @Valid @RequestBody PaymentRequest request) {
        try {
            var contribution = payoutCycleService.recordPayment(
                    payoutCycleId, request.getUserId(), request.getAmount());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Payment recorded successfully");
            response.put("contribution", contribution);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // ── Read ─────────────────────────────────────────────────────────────────

    /**
     * Get payment collection progress for a payout cycle.
     *
     * <pre>GET /api/payouts/{payoutCycleId}/progress</pre>
     *
     * @param payoutCycleId the payout cycle's ID
     * @return member count, paid count, amounts expected/collected, and percentage
     */
    @GetMapping("/{payoutCycleId}/progress")
    public ResponseEntity<?> getPaymentProgress(@PathVariable Long payoutCycleId) {
        try {
            PaymentProgressResponse progress =
                    payoutCycleService.getPaymentProgress(payoutCycleId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("progress", progress);
            response.put("paymentPercentage", progress.getPaymentPercentage());

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Get all contributions recorded for a payout cycle.
     *
     * <pre>GET /api/payouts/{payoutCycleId}/contributions</pre>
     *
     * @param payoutCycleId the payout cycle's ID
     * @return list of all contributions with their statuses
     */
    @GetMapping("/{payoutCycleId}/contributions")
    public ResponseEntity<?> getCycleContributions(@PathVariable Long payoutCycleId) {
        try {
            var contributions = payoutCycleService.getContributionsForPayoutCycle(payoutCycleId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("contributions", contributions);
            response.put("count", contributions.size());

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}
