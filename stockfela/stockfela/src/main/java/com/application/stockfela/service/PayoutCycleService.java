package com.application.stockfela.service;

import com.application.stockfela.CustomException.ResourceNotFoundException;
import com.application.stockfela.dto.response.PaymentProgressResponse;
import com.application.stockfela.entity.Contribution;
import com.application.stockfela.entity.GroupMember;
import com.application.stockfela.entity.PayoutCycle;
import com.application.stockfela.entity.SavingsGroup;
import com.application.stockfela.repository.ContributionRepository;
import com.application.stockfela.repository.GroupMemberRepository;
import com.application.stockfela.repository.PayoutCycleRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service layer for payout-cycle and contribution operations.
 *
 * <p>A payout cycle represents one month in a savings group where:
 * <ol>
 *   <li>Every member contributes their fixed monthly amount.</li>
 *   <li>The designated recipient receives the pooled total.</li>
 * </ol>
 *
 * <p>This service handles creating cycles, recording individual payments,
 * and automatically completing a cycle once all contributions are paid.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class PayoutCycleService {

    /** Repository for {@link PayoutCycle} persistence. */
    private final PayoutCycleRepository payoutCycleRepository;

    /** Repository for {@link Contribution} persistence. */
    private final ContributionRepository contributionRepository;

    /** Repository for looking up group members when creating contributions. */
    private final GroupMemberRepository groupMemberRepository;

    // ── Payout cycle CRUD ────────────────────────────────────────────────────

    /**
     * Persist a new or updated {@link PayoutCycle}.
     *
     * @param payoutCycle the cycle to save
     * @return the saved entity (ID populated after first save)
     */
    public PayoutCycle savePayoutCycle(PayoutCycle payoutCycle) {
        return payoutCycleRepository.save(payoutCycle);
    }

    /**
     * Look up a payout cycle by its primary key.
     *
     * @param id the payout cycle ID
     * @return an {@link Optional} with the cycle if found
     */
    public Optional<PayoutCycle> findById(Long id) {
        return payoutCycleRepository.findById(id);
    }

    /**
     * Get all payout cycles for a group, most recent first.
     *
     * @param groupId the savings group ID
     * @return list of cycles ordered by cycle number descending
     */
    public List<PayoutCycle> getPayoutCycleByGroup(Long groupId) {
        return payoutCycleRepository.findByGroupIdOrderByCycleNumberDesc(groupId);
    }

    /**
     * Get the current (latest) payout cycle for a group.
     *
     * @param groupId the savings group ID
     * @return an {@link Optional} with the most recent cycle, or empty
     */
    public Optional<PayoutCycle> getCurrentPayoutCycle(Long groupId) {
        return payoutCycleRepository.findCurrentPayoutCycle(groupId);
    }

    // ── Contribution management ──────────────────────────────────────────────

    /**
     * Create a {@link Contribution#ContributionStatus#PENDING PENDING}
     * contribution record for every active member of the group when a new
     * payout cycle starts.
     *
     * <p>Each contribution is for the group's fixed
     * {@link SavingsGroup#getMonthlyContribution() monthlyContribution} amount.
     *
     * @param payoutCycle the newly created payout cycle
     */
    public void createContributionForPayoutCycle(PayoutCycle payoutCycle) {
        SavingsGroup group = payoutCycle.getGroup();
        BigDecimal contributionAmount = group.getMonthlyContribution();

        List<GroupMember> members = groupMemberRepository.findByGroup(group);

        for (GroupMember member : members) {
            Contribution contribution = new Contribution(
                    group, member.getUser(), payoutCycle, contributionAmount);
            contribution.setStatus(Contribution.ContributionStatus.PENDING);
            contributionRepository.save(contribution);
        }
    }

    /**
     * Record that a member has made their payment for a payout cycle.
     *
     * <p>Marks the contribution as {@link Contribution.ContributionStatus#PAID}
     * and then checks whether all contributions for the cycle are now paid;
     * if so, the cycle is automatically marked {@link PayoutCycle.PayoutStatus#COMPLETED}.
     *
     * @param payoutCycleId the ID of the active payout cycle
     * @param userId        the ID of the user making the payment
     * @param amount        the amount paid (recorded for auditing)
     * @return the updated {@link Contribution} entity
     * @throws RuntimeException if no pending contribution is found for this user/cycle
     */
    public Contribution recordPayment(Long payoutCycleId, Long userId, BigDecimal amount) {
        Contribution contribution = contributionRepository
                .findByPayoutCycleIdAndUserId(payoutCycleId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Contribution not found for cycle " + payoutCycleId
                        + " and user " + userId));

        contribution.setStatus(Contribution.ContributionStatus.PAID);
        contribution.setAmount(amount);
        contribution.setPaymentDate(LocalDate.now());
        contribution.setPaidAt(LocalDateTime.now());

        Contribution saved = contributionRepository.save(contribution);
        checkAndCompletePayoutCycle(payoutCycleId);
        return saved;
    }

    /**
     * Retrieve all contributions recorded for a given payout cycle.
     *
     * @param payoutCycleId the cycle's ID
     * @return list of all contributions (paid and pending)
     */
    public List<Contribution> getContributionsForPayoutCycle(Long payoutCycleId) {
        return contributionRepository.findByPayoutCycleId(payoutCycleId);
    }

    /**
     * Retrieve a specific user's contribution for a given payout cycle.
     *
     * @param payoutCycleId the cycle's ID
     * @param userId        the user's ID
     * @return an {@link Optional} with the contribution if it exists
     */
    public Optional<Contribution> getUserContributionForCycle(Long payoutCycleId, Long userId) {
        return contributionRepository.findByPayoutCycleIdAndUserId(payoutCycleId, userId);
    }

    // ── Progress reporting ───────────────────────────────────────────────────

    /**
     * Calculate total amount collected (sum of all PAID contributions)
     * for a payout cycle.
     *
     * @param payoutCycleId the cycle's ID
     * @return total collected; {@link BigDecimal#ZERO} if nothing paid yet
     */
    public BigDecimal getTotalCollectedAmount(Long payoutCycleId) {
        return contributionRepository
                .findByPayoutCycleIdAndStatus(payoutCycleId, Contribution.ContributionStatus.PAID)
                .stream()
                .map(Contribution::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Build a {@link PaymentProgressResponse} summarising how many members
     * have paid and how much has been collected for a given cycle.
     *
     * @param payoutCycleId the cycle's ID
     * @return a progress snapshot DTO
     * @throws RuntimeException if the payout cycle does not exist
     */
    public PaymentProgressResponse getPaymentProgress(Long payoutCycleId) {
        PayoutCycle payoutCycle = payoutCycleRepository.findById(payoutCycleId)
                .orElseThrow(() -> new RuntimeException(
                        "Payout cycle not found with id: " + payoutCycleId));

        List<Contribution> all = contributionRepository.findByPayoutCycleId(payoutCycleId);
        long paidMembers = all.stream()
                .filter(c -> c.getStatus() == Contribution.ContributionStatus.PAID)
                .count();

        return new PaymentProgressResponse(
                all.size(),
                paidMembers,
                payoutCycle.getTotalAmount(),
                getTotalCollectedAmount(payoutCycleId));
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    /**
     * Check whether all contributions for a cycle have been paid and, if so,
     * advance the cycle status to {@link PayoutCycle.PayoutStatus#COMPLETED}.
     *
     * @param payoutCycleId the cycle to check
     */
    private void checkAndCompletePayoutCycle(Long payoutCycleId) {
        PayoutCycle payoutCycle = payoutCycleRepository.findById(payoutCycleId)
                .orElseThrow(() -> new RuntimeException(
                        "Payout cycle not found with id: " + payoutCycleId));

        long pendingCount = contributionRepository.countByPayoutCycleAndStatus(
                payoutCycle, Contribution.ContributionStatus.PENDING);

        if (pendingCount == 0) {
            payoutCycle.setStatus(PayoutCycle.PayoutStatus.COMPLETED);
            payoutCycleRepository.save(payoutCycle);
        }
    }
}
