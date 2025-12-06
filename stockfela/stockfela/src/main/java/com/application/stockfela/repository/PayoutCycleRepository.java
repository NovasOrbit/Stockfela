package com.application.stockfela.repository;

import com.application.stockfela.entity.PayoutCycle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PayoutCycleRepository extends JpaRepository<PayoutCycle, Long> {

    // Find all payout cycles for a group, ordered by cycle number
    List<PayoutCycle> findByGroupIdOrderByCycleNumberDesc(Long groupId);

    // Find the current (most recent) payout cycle for a group
    @Query("SELECT pc FROM PayoutCycle pc WHERE pc.group.id = :groupId ORDER BY pc.cycleNumber DESC LIMIT 1")
    Optional<PayoutCycle> findCurrentPayoutCycle(@Param("groupId") Long groupId);

    // Find payout cycles by status
    List<PayoutCycle> findByStatus(PayoutCycle.PayoutStatus status);

    // Check if a payout cycle exists for a group and cycle number
    boolean existsByGroupIdAndCycleNumber(Long groupId, Integer cycleNumber);
}