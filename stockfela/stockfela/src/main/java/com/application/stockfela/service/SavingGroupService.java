package com.application.stockfela.service;

import com.application.stockfela.entity.GroupMember;
import com.application.stockfela.entity.PayoutCycle;
import com.application.stockfela.entity.SavingsGroup;
import com.application.stockfela.entity.User;
import com.application.stockfela.repository.GroupMemberRepository;
import com.application.stockfela.repository.SavingsGroupRepository;
import com.application.stockfela.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Service layer for savings-group lifecycle management.
 *
 * <p>Orchestrates the main ROSCA workflow:
 * <ol>
 *   <li>Creating groups and adding members.</li>
 *   <li>Starting payout cycles (determining the next recipient, creating
 *       contribution records, advancing the cycle counter).</li>
 *   <li>Querying group and member data.</li>
 * </ol>
 *
 * <p>All public methods are transactional: if any step in a workflow throws,
 * the entire operation is rolled back to keep the database consistent.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class SavingGroupService {

    /** Repository for {@link SavingsGroup} persistence. */
    private final SavingsGroupRepository savingsGroupRepository;

    /** Repository for {@link GroupMember} join-entity persistence. */
    private final GroupMemberRepository groupMemberRepository;

    /** Repository used to look up users by ID when adding members. */
    private final UserRepository userRepository;

    /** Handles contribution creation and cycle completion checks. */
    private final PayoutCycleService payoutCycleService;

    // ── Group lifecycle ──────────────────────────────────────────────────────

    /**
     * Create a new savings group and automatically enrol the creator
     * as the first member (payout order = 1).
     *
     * @param group   a transient {@link SavingsGroup} entity populated by the controller
     * @param creator the authenticated user creating the group
     * @return the persisted group (with its generated {@code id})
     */
    public SavingsGroup createGroup(SavingsGroup group, User creator) {
        group.setCreatedBy(creator);
        SavingsGroup savedGroup = savingsGroupRepository.save(group);

        // Automatically add the creator as the first member
        GroupMember creatorMember = new GroupMember(savedGroup, creator, 1);
        groupMemberRepository.save(creatorMember);

        return savedGroup;
    }

    /**
     * Update mutable fields of an existing group (name, description,
     * monthly contribution). Does not allow changing cycleMonths or members.
     *
     * @param groupId      the ID of the group to update
     * @param groupDetails a partial entity containing only the fields to update
     * @return the updated group
     * @throws RuntimeException if no group exists with the given ID
     */
    public SavingsGroup updateGroup(Long groupId, SavingsGroup groupDetails) {
        SavingsGroup group = savingsGroupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found with id: " + groupId));

        if (groupDetails.getName() != null) {
            group.setName(groupDetails.getName());
        }
        if (groupDetails.getDescription() != null) {
            group.setDescription(groupDetails.getDescription());
        }
        if (groupDetails.getMonthlyContribution() != null) {
            group.setMonthlyContribution(groupDetails.getMonthlyContribution());
        }

        return savingsGroupRepository.save(group);
    }

    // ── Member management ────────────────────────────────────────────────────

    /**
     * Add a user to an existing savings group.
     *
     * @param groupId     the target group's ID
     * @param userId      the user's ID
     * @param payoutOrder the user's position in the payout queue (1-based)
     * @return the created {@link GroupMember} entity
     * @throws RuntimeException if the group or user does not exist,
     *                          or the user is already a member
     */
    public GroupMember addMemberToGroup(Long groupId, Long userId, Integer payoutOrder) {
        SavingsGroup group = savingsGroupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found with id: " + groupId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        if (groupMemberRepository.findByGroupAndUser(group, user).isPresent()) {
            throw new RuntimeException("User is already a member of this group");
        }

        return groupMemberRepository.save(new GroupMember(group, user, payoutOrder));
    }

    /**
     * Remove a user from a savings group.
     *
     * @param groupId the group's ID
     * @param userId  the user's ID
     * @throws RuntimeException if the group, user, or membership record is not found
     */
    public void removeMemberFromGroup(Long groupId, Long userId) {
        SavingsGroup group = savingsGroupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found with id: " + groupId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        GroupMember member = groupMemberRepository.findByGroupAndUser(group, user)
                .orElseThrow(() -> new RuntimeException("Member not found in group"));

        groupMemberRepository.delete(member);
    }

    // ── Payout cycle ─────────────────────────────────────────────────────────

    /**
     * Start the next payout cycle for a group.
     *
     * <p>Steps performed in a single transaction:
     * <ol>
     *   <li>Determine the next recipient (lowest payout-order member who
     *       hasn't yet received a payout).</li>
     *   <li>Calculate the total payout amount
     *       ({@code memberCount × monthlyContribution}).</li>
     *   <li>Create the {@link PayoutCycle} entity and persist it.</li>
     *   <li>Create {@link com.application.stockfela.entity.Contribution} records
     *       for all members (initial status: PENDING).</li>
     *   <li>Mark the recipient as having received a payout.</li>
     *   <li>Increment the group's current cycle counter; mark the group
     *       COMPLETED if all cycles are done.</li>
     * </ol>
     *
     * @param groupId the group to advance
     * @return the newly created and persisted {@link PayoutCycle}
     * @throws RuntimeException if the group is not found or all members have
     *                          already received their payouts
     */
    public PayoutCycle startNewPayoutCycle(Long groupId) {
        SavingsGroup group = savingsGroupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found with id: " + groupId));

        List<GroupMember> nextRecipients = groupMemberRepository.findNextPayoutRecipient(groupId);
        if (nextRecipients.isEmpty()) {
            throw new RuntimeException(
                    "All members have received their payout or no members in group");
        }

        GroupMember nextRecipient = nextRecipients.get(0);
        Long memberCount = groupMemberRepository.countByGroup(group);
        BigDecimal totalAmount = group.getMonthlyContribution()
                .multiply(BigDecimal.valueOf(memberCount));

        // Build and persist the payout cycle
        PayoutCycle payoutCycle = new PayoutCycle();
        payoutCycle.setGroup(group);
        payoutCycle.setCycleNumber(group.getCurrentCycle());
        payoutCycle.setRecipientUser(nextRecipient.getUser());
        payoutCycle.setTotalAmount(totalAmount);
        payoutCycle.setPayoutDate(LocalDate.now());
        payoutCycle.setStatus(PayoutCycle.PayoutStatus.PENDING);

        PayoutCycle savedCycle = payoutCycleService.savePayoutCycle(payoutCycle);

        // Create pending contribution records for all members
        payoutCycleService.createContributionForPayoutCycle(savedCycle);

        // Mark recipient as paid out so they aren't selected again
        nextRecipient.setHasReceivedPayout(true);
        groupMemberRepository.save(nextRecipient);

        // Advance cycle counter and complete group if all cycles done
        group.setCurrentCycle(group.getCurrentCycle() + 1);
        if (group.getCurrentCycle() > group.getCycleMonths()) {
            group.setStatus(SavingsGroup.GroupStatus.COMPLETED);
        }
        savingsGroupRepository.save(group);

        return savedCycle;
    }

    // ── Queries ──────────────────────────────────────────────────────────────

    /**
     * Get all groups the given user belongs to (as creator or added member).
     *
     * @param userId the user's ID
     * @return list of groups (may be empty)
     */
    public List<SavingsGroup> getUserGroups(Long userId) {
        return savingsGroupRepository.findGroupByMemberId(userId);
    }

    /**
     * Get a group with its members and payout cycles loaded.
     *
     * @param groupId the group's ID
     * @return the group entity
     * @throws RuntimeException if no group exists with the given ID
     */
    public SavingsGroup getGroupWithMembers(Long groupId) {
        return savingsGroupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found with id: " + groupId));
    }

    /**
     * Get all members of a group.
     *
     * @param groupId the group's ID
     * @return list of group member records
     * @throws RuntimeException if no group exists with the given ID
     */
    public List<GroupMember> getGroupMembers(Long groupId) {
        SavingsGroup group = savingsGroupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found with id: " + groupId));
        return groupMemberRepository.findByGroup(group);
    }

    /**
     * Get all groups created by a specific user.
     *
     * @param userId the creator's ID
     * @return list of groups created by the user
     * @throws RuntimeException if no user exists with the given ID
     */
    public List<SavingsGroup> getGroupsCreatedByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        return savingsGroupRepository.findByCreatedBy(user);
    }

    /**
     * Check whether a given user is a member of a given group.
     *
     * @param groupId the group's ID
     * @param userId  the user's ID
     * @return {@code true} if a {@link GroupMember} record exists for this pair
     */
    public boolean isUserMemberOfGroup(Long groupId, Long userId) {
        SavingsGroup group = savingsGroupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found with id: " + groupId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        return groupMemberRepository.findByGroupAndUser(group, user).isPresent();
    }
}
