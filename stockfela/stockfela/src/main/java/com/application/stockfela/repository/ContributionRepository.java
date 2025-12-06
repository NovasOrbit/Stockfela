package com.application.stockfela.repository;

import com.application.stockfela.entity.Contribution;
import com.application.stockfela.entity.PayoutCycle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ContributionRepository extends JpaRepository<Contribution, Long> {

    // Find all contributions for a payout cycle
    List<Contribution> findByPayoutCycleId(Long payoutCycleId);

    // Find contributions by status for a payout cycle
    List<Contribution> findByPayoutCycleIdAndStatus(Long payoutCycleId, Contribution.ContributionStatus status);

    // Find a specific user's contribution for a payout cycle
    @Query("SELECT c FROM Contribution c WHERE c.payoutCycle.id = :payoutCycleId AND c.user.id = :userId")
    Optional<Contribution> findByPayoutCycleIdAndUserId(@Param("payoutCycleId") Long payoutCycleId,
                                                        @Param("userId") Long userId);

    // Count contributions by status for a payout cycle
    long countByPayoutCycleAndStatus(PayoutCycle payoutCycle, Contribution.ContributionStatus status);

    // Find all contributions by a user across all groups
    List<Contribution> findByUserId(Long userId);

    // Find user's contributions for a specific group
    List<Contribution> findByUserIdAndGroupId(Long userId, Long groupId);
}