package com.application.stockfela.service;

import com.application.stockfela.entity.Contribution;
import com.application.stockfela.entity.GroupMember;
import com.application.stockfela.entity.PayoutCycle;
import com.application.stockfela.entity.SavingsGroup;
import com.application.stockfela.repository.ContributionRepository;
import com.application.stockfela.repository.GroupMemberRepository;
import com.application.stockfela.repository.PayoutCycleRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PayoutCycleService {

    @Autowired
    private PayoutCycleRepository payoutCycleRepository;

    @Autowired
    private ContributionRepository contributionRepository;

    @Autowired
    private GroupMemberRepository groupMemberRepository;

    //Save a payout cycle (create new or update existing)
    public PayoutCycle savePayoutCycle(PayoutCycle payoutCycle) {
        return payoutCycleRepository.save(payoutCycle);
    }

    //Get payment cycle by ID
    public Optional<PayoutCycle> findById(Long id) {
        return payoutCycleRepository.findById(id);
    }

    //Get payment cycle for a group
    public List<PayoutCycle> getPayoutCycleByGroup(Long groupId) {
        return payoutCycleRepository.findByGroupIdOrderByCycleNumberDesc(groupId);
    }

    //Get current active payout cycle for a group
    public Optional<PayoutCycle> getCurrentPayoutCycle(Long groupId) {
        return payoutCycleRepository.findCurrentPayoutCycle(groupId);
    }

    //Create contributions for all members when starting a new payout cycle
    //This automatically creates pending contributions for each member
    public void createContributionForPayoutCycle(PayoutCycle payoutCycle) {
        SavingsGroup group = payoutCycle.getGroup();
        BigDecimal contributionAmount = group.getMonthlyContribution();

        //Get all active members in the group
        List<GroupMember> members = groupMemberRepository.findByGroup(group);

        for (GroupMember member : members) {
            Contribution contribution = new Contribution();
            contribution.setGroup(group);
            contribution.setUser(member.getUser());
            contribution.setPayoutCycle(payoutCycle);
            contribution.setAmount(contributionAmount);
            contribution.setStatus(Contribution.ContributionStatus.PENDING);

            contributionRepository.save(contribution);
        }
    }

    //Record a member's payment for the current payment cycle
    public Contribution recordPayment(Long payoutCycleId, Long userId, BigDecimal amount) {
        //find the contribution for this user in this payout cycle
        Contribution contribution = contributionRepository
                .findByPayoutCycleIdAndUserId(payoutCycleId, userId)
                .orElseThrow(() -> new RuntimeException("Contribution not found"));

        //Update contribution status
        contribution.setStatus(Contribution.ContributionStatus.PAID);
        contribution.setPaymentDate(LocalDate.now());
        contribution.setPaidAt(java.time.LocalDateTime.now());

        Contribution savedContribution = contributionRepository.save(contribution);

        // Check if all contributions are now paid
        checkAndCompletePayoutCycle(payoutCycleId);

        return savedContribution;
    }

    //Check if all contributions for a payout cycle are paid and marked cycle as completed
    private void checkAndCompletePayoutCycle(Long payoutCycleId) {
        PayoutCycle payoutCycle = payoutCycleRepository.findById(payoutCycleId)
                .orElseThrow(() -> new RuntimeException("Payout cycle not found"));

        //Count pending contributions
        long pendingCount = contributionRepository.countByPayoutCycleAndStatus(
                payoutCycle, Contribution.ContributionStatus.PENDING);

        if (pendingCount == 0) {
            //All contributions are paid, mark payout cycle as completed
            payoutCycle.setStatus(PayoutCycle.PayoutStatus.COMPLETED);
            payoutCycleRepository.save(payoutCycle);
        }
    }

    //Get all contributions for a payout cycle
    public List<Contribution> getContributionsForPayoutCycle(Long payoutCycleId) {
        return contributionRepository.findByPayoutCycleId(payoutCycleId);
    }

    //Get a user's contributions for a specific payout cycle
    public Optional<Contribution> getUserContributionForCycle(Long payoutCycleId, Long userId) {
        return contributionRepository.findByPayoutCycleIdAndUserId(payoutCycleId, userId);
    }

    //Calculate total amount collected for a payout cycle
    public BigDecimal getTotalCollectedAmount(Long payoutCycleId) {
        List<Contribution> paidContributions = contributionRepository
                .findByPayoutCycleIdAndStatus(payoutCycleId, Contribution.ContributionStatus.PAID);
        return paidContributions.stream()
                .map(Contribution::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Get payment progress for payout cycle
    public PaymentProgress getPaymentProgress(Long payoutCycleId) {
        PayoutCycle payoutCycle = payoutCycleRepository.findById(payoutCycleId)
                .orElseThrow(() -> new RuntimeException("Payout cycle not found"));

        List<Contribution> allContributions = contributionRepository.findByPayoutCycleId(payoutCycleId);
        long totalMembers = allContributions.size();
        long paidMembers = allContributions.stream()
                .filter(c -> c.getStatus() == Contribution.ContributionStatus.PAID)
                .count();

        BigDecimal totalExpected = payoutCycle.getTotalAmount();
        BigDecimal totalCollected = getTotalCollectedAmount(payoutCycleId);

        return new PaymentProgress(totalMembers, paidMembers, totalExpected, totalCollected);
    }

    /**
     * DTO for payment progress information
     */
    public static class PaymentProgress {
        private final long totalMembers;
        private final long paidMembers;
        private final BigDecimal totalExpected;
        private final BigDecimal totalCollected;

        public PaymentProgress(long totalMembers, long paidMembers,
                               BigDecimal totalExpected, BigDecimal totalCollected) {
            this.totalMembers = totalMembers;
            this.paidMembers = paidMembers;
            this.totalExpected = totalExpected;
            this.totalCollected = totalCollected;
        }

        // Getters
        public long getTotalMembers() { return totalMembers; }
        public long getPaidMembers() { return paidMembers; }
        public BigDecimal getTotalExpected() { return totalExpected; }
        public BigDecimal getTotalCollected() { return totalCollected; }

        public double getPaymentPercentage() {
            if (totalMembers == 0) return 0.0;
            return (double) paidMembers / totalMembers * 100.0;
        }
    }
}








