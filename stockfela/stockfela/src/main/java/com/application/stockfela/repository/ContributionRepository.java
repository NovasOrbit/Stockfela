package com.application.stockfela.repository;

import com.application.stockfela.entity.Contribution;
import com.application.stockfela.entity.PayoutCycle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link Contribution} entities.
 *
 * <p>Contributions are the individual payment records created when a payout
 * cycle starts. This repository supports the primary use cases:
 * <ul>
 *   <li>Listing all contributions for a cycle (to show payment status).</li>
 *   <li>Finding a specific user's contribution to record their payment.</li>
 *   <li>Counting pending contributions to determine cycle completion.</li>
 * </ul>
 */
@Repository
public interface ContributionRepository extends JpaRepository<Contribution, Long> {

    /**
     * Find all contributions belonging to a payout cycle.
     *
     * @param payoutCycleId the payout cycle's ID
     * @return list of all contributions (paid and pending)
     */
    List<Contribution> findByPayoutCycleId(Long payoutCycleId);

    /**
     * Find contributions for a payout cycle filtered by status.
     * Commonly used to sum paid amounts or list outstanding contributions.
     *
     * @param payoutCycleId the payout cycle's ID
     * @param status        the status to filter by
     * @return list of contributions matching the criteria
     */
    List<Contribution> findByPayoutCycleIdAndStatus(
            Long payoutCycleId, Contribution.ContributionStatus status);

    /**
     * Find a specific user's contribution for a given payout cycle.
     * Used when recording a payment to locate the exact row to update.
     *
     * @param payoutCycleId the payout cycle's ID
     * @param userId        the user's ID
     * @return an {@link Optional} with the contribution if found
     */
    @Query("SELECT c FROM Contribution c " +
           "WHERE c.payoutCycle.id = :payoutCycleId AND c.user.id = :userId")
    Optional<Contribution> findByPayoutCycleIdAndUserId(
            @Param("payoutCycleId") Long payoutCycleId,
            @Param("userId") Long userId);

    /**
     * Count contributions for a payout cycle with a given status.
     * Called after each payment to check if all contributions are PAID
     * and the cycle can be marked COMPLETED.
     *
     * @param payoutCycle the payout cycle entity
     * @param status      the status to count
     * @return number of contributions in the given status
     */
    long countByPayoutCycleAndStatus(
            PayoutCycle payoutCycle, Contribution.ContributionStatus status);

    /**
     * Find all contributions made by a user across all groups and cycles.
     * Useful for a user's contribution history view.
     *
     * @param userId the user's ID
     * @return list of all contributions by this user
     */
    List<Contribution> findByUserId(Long userId);

    /**
     * Find a user's contributions for a specific group.
     * Used to generate a member's payment history within one group.
     *
     * @param userId  the user's ID
     * @param groupId the group's ID
     * @return list of contributions by this user in this group
     */
    List<Contribution> findByUserIdAndGroupId(Long userId, Long groupId);
}
