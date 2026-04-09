package com.application.stockfela.controller;

import com.application.stockfela.dto.request.PaymentRequest;
import com.application.stockfela.service.PayoutCycleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payouts")
public class PayoutController {

    @Autowired
    private PayoutCycleService payoutCycleService;

    /**
     * RECORD A MEMBER'S PAYMENT
     * POST http://localhost:8080/api/payouts/1/pay
     * {
     *   "userId": 2,
     *   "amount": 1000.00
     * }
     */
    @PostMapping("/{payoutCycleId}/pay")
    public ResponseEntity<?> recordPayment(@PathVariable Long payoutCycleId, @RequestBody PaymentRequest request) {
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

    /**
     * GET PAYMENT PROGRESS FOR A PAYOUT CYCLE
     * GET http://localhost:8080/api/payouts/1/progress
     */
    @GetMapping("/{payoutCycleId}/progress")
    public ResponseEntity<?> getPaymentProgress(@PathVariable Long payoutCycleId) {
        try {
            var progress = payoutCycleService.getPaymentProgress(payoutCycleId);

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
     * GET ALL CONTRIBUTIONS FOR A PAYOUT CYCLE
     * GET http://localhost:8080/api/payouts/1/contributions
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