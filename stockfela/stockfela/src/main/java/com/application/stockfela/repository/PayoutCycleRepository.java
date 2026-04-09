package com.application.stockfela.repository;

import com.application.stockfela.entity.PayoutCycle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link PayoutCycle} entities.
 *
 * <p>Supports querying the payout history for a group and determining which
 * cycle is currently active.
 */
@Repository
public interface PayoutCycleRepository extends JpaRepository<PayoutCycle, Long> {

    /**
     * Find all payout cycles for a group, ordered by cycle number descending
     * so the most recent cycle appears first.
     *
     * @param groupId the savings group's ID
     * @return list of payout cycles, most recent first
     */
    List<PayoutCycle> findByGroupIdOrderByCycleNumberDesc(Long groupId);

    /**
     * Find the current (highest cycle number) payout cycle for a group.
     * {@code LIMIT 1} with {@code DESC} ordering returns the latest entry.
     *
     * @param groupId the savings group's ID
     * @return an {@link Optional} with the most recent payout cycle
     */
    @Query("SELECT pc FROM PayoutCycle pc " +
           "WHERE pc.group.id = :groupId " +
           "ORDER BY pc.cycleNumber DESC LIMIT 1")
    Optional<PayoutCycle> findCurrentPayoutCycle(@Param("groupId") Long groupId);

    /**
     * Find all payout cycles with a given status across all groups.
     * Useful for administrative views (e.g. listing all pending cycles).
     *
     * @param status the {@link PayoutCycle.PayoutStatus} to filter by
     * @return list of matching payout cycles
     */
    List<PayoutCycle> findByStatus(PayoutCycle.PayoutStatus status);

    /**
     * Check whether a payout cycle already exists for the given group and
     * cycle number. Prevents creating duplicate cycles.
     *
     * @param groupId     the savings group's ID
     * @param cycleNumber the cycle number to check
     * @return {@code true} if a cycle already exists for this group/number pair
     */
    boolean existsByGroupIdAndCycleNumber(Long groupId, Integer cycleNumber);
}
