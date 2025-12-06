package com.application.stockfela.service;

import com.application.stockfela.entity.GroupMember;
import com.application.stockfela.entity.PayoutCycle;
import com.application.stockfela.entity.SavingsGroup;
import com.application.stockfela.entity.User;
import com.application.stockfela.repository.GroupMemberRepository;
import com.application.stockfela.repository.SavingsGroupRepository;
import com.application.stockfela.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class SavingGroupService {

    @Autowired
    private SavingsGroupRepository savingsGroupRepository;

    @Autowired
    private GroupMemberRepository groupMemberRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PayoutCycleService payoutCycleService;

    /**
     * Create a new savings group and add the creator as first member
     */
    public SavingsGroup createGroup(SavingsGroup group, User creator) {
        // Set the creator
        group.setCreatedBy(creator);

        // Save the group first
        SavingsGroup savedGroup = savingsGroupRepository.save(group);

        // Add creator as first member with payout order 1
        GroupMember creatorMember = new GroupMember(savedGroup, creator, 1);
        groupMemberRepository.save(creatorMember); // FIXED: Missing save operation

        return savedGroup;
    }

    /**
     * Add a user to an existing group
     */
    public GroupMember addMemberToGroup(Long groupId, Long userId, Integer payoutOrder) {
        SavingsGroup group = savingsGroupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if user is already in group
        if (groupMemberRepository.findByGroupAndUser(group, user).isPresent()) {
            throw new RuntimeException("User is already a member of this group");
        }

        // FIXED: Missing return statement - create and save the new group member
        GroupMember newMember = new GroupMember(group, user, payoutOrder);
        return groupMemberRepository.save(newMember);
    }

    /**
     * Get all groups for a user (both created and joined)
     */
    public List<SavingsGroup> getUserGroups(Long userId) {
        return savingsGroupRepository.findGroupByMemberId(userId);
    }

    /**
     * Get group details with members
     */
    public SavingsGroup getGroupWithMembers(Long groupId) {
        return savingsGroupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));
    }

    /**
     * Start a new payout cycle for a group with complete flow
     */
    public PayoutCycle startNewPayoutCycle(Long groupId) {
        SavingsGroup group = savingsGroupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        // Find next member to receive payout
        List<GroupMember> nextRecipients = groupMemberRepository.findNextPayoutRecipient(groupId);
        if (nextRecipients.isEmpty()) {
            throw new RuntimeException("All members have received payout or no members in group");
        }

        GroupMember nextRecipient = nextRecipients.get(0);

        // Calculate total payout amount (number of members * monthly contribution)
        Long memberCount = groupMemberRepository.countByGroup(group);
        BigDecimal totalAmount = group.getMonthlyContribution().multiply(BigDecimal.valueOf(memberCount));

        // Create payout cycle
        PayoutCycle payoutCycle = new PayoutCycle();
        payoutCycle.setGroup(group);
        payoutCycle.setCycleNumber(group.getCurrentCycle());
        payoutCycle.setRecipientUser(nextRecipient.getUser());
        payoutCycle.setTotalAmount(totalAmount);
        payoutCycle.setPayoutDate(LocalDate.now());
        payoutCycle.setStatus(PayoutCycle.PayoutStatus.PENDING);

        // Save the payout cycle first
        PayoutCycle savedPayoutCycle = payoutCycleService.savePayoutCycle(payoutCycle);

        // Create contributions for all members
        payoutCycleService.createContributionForPayoutCycle(savedPayoutCycle);

        // Mark member as having received payout
        nextRecipient.setHasReceivedPayout(true);
        groupMemberRepository.save(nextRecipient);

        // Update group cycle
        group.setCurrentCycle(group.getCurrentCycle() + 1);

        // Check if group has completed all cycles
        if (group.getCurrentCycle() > group.getCycleMonths()) {
            group.setStatus(SavingsGroup.GroupStatus.COMPLETED);
        }

        savingsGroupRepository.save(group);

        return savedPayoutCycle;
    }

    /**
     * Get all members of a group
     */
    public List<GroupMember> getGroupMembers(Long groupId) {
        SavingsGroup group = savingsGroupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));
        return groupMemberRepository.findByGroup(group);
    }

    /**
     * Remove member from group
     */
    public void removeMemberFromGroup(Long groupId, Long userId) {
        SavingsGroup group = savingsGroupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        GroupMember member = groupMemberRepository.findByGroupAndUser(group, user)
                .orElseThrow(() -> new RuntimeException("Member not found in group"));

        groupMemberRepository.delete(member);
    }

    /**
     * Update group information
     */
    public SavingsGroup updateGroup(Long groupId, SavingsGroup groupDetails) {
        SavingsGroup group = savingsGroupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        // Update allowed fields
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

    /**
     * Get groups created by a specific user
     */
    public List<SavingsGroup> getGroupsCreatedByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return savingsGroupRepository.findByCreatedBy(user);
    }

    /**
     * Check if user is member of group
     */
    public boolean isUserMemberOfGroup(Long groupId, Long userId) {
        SavingsGroup group = savingsGroupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return groupMemberRepository.findByGroupAndUser(group, user).isPresent();
    }
}