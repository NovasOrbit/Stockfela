package com.application.stockfela.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * JPA entity representing a user's membership in a savings group.
 *
 * <p>This is a join entity between {@link User} and {@link SavingsGroup}.
 * It stores membership-specific data:
 * <ul>
 *   <li>The user's payout order within the group (who gets paid first, second, etc.).</li>
 *   <li>Whether this member has already received the group payout this rotation.</li>
 * </ul>
 *
 * <p>When a group is created the founding user automatically gets a
 * {@code GroupMember} record with {@code payoutOrder = 1}.
 *
 * <p>Mapped to the {@code group_members} table.
 */
@Entity
@Table(name = "group_members")
public class GroupMember {

    // ── Primary key ──────────────────────────────────────────────────────────

    /** Auto-generated database primary key. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── Relationships ────────────────────────────────────────────────────────

    /**
     * The savings group this membership record belongs to.
     * {@code @JsonIgnoreProperties} prevents: Member → Group → Members loop.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    @JsonIgnoreProperties({"members", "payoutCycles", "createdBy"})
    private SavingsGroup group;

    /**
     * The user who holds this membership.
     * {@code @JsonIgnoreProperties} prevents: Member → User → Memberships loop.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"groupMemberships", "createdGroups", "password"})
    private User user;

    // ── Membership fields ────────────────────────────────────────────────────

    /**
     * The 1-based position in the payout queue.
     * Member with {@code payoutOrder = 1} receives the payout in the first cycle,
     * {@code payoutOrder = 2} in the second, and so on.
     */
    @Column(name = "payout_order", nullable = false)
    private Integer payoutOrder;

    /**
     * Flag set to {@code true} once this member has received their payout.
     * Used by {@link com.application.stockfela.repository.GroupMemberRepository#findNextPayoutRecipient}
     * to determine who is next in line.
     */
    @Column(name = "has_received_payout")
    private Boolean hasReceivedPayout = false;

    // ── Audit ────────────────────────────────────────────────────────────────

    /** Timestamp set automatically when the member joined the group. */
    @CreationTimestamp
    @Column(name = "joined_at", updatable = false)
    private LocalDateTime joinedAt;

    // ── Constructors ─────────────────────────────────────────────────────────

    /** Required by JPA. */
    public GroupMember() {}

    /**
     * Convenience constructor used when adding a member to a group.
     *
     * @param group       the savings group
     * @param user        the user to add
     * @param payoutOrder the user's position in the payout queue
     */
    public GroupMember(SavingsGroup group, User user, Integer payoutOrder) {
        this.group = group;
        this.user = user;
        this.payoutOrder = payoutOrder;
    }

    // ── Getters and Setters ──────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public SavingsGroup getGroup() { return group; }
    public void setGroup(SavingsGroup group) { this.group = group; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Integer getPayoutOrder() { return payoutOrder; }
    public void setPayoutOrder(Integer payoutOrder) { this.payoutOrder = payoutOrder; }

    public Boolean getHasReceivedPayout() { return hasReceivedPayout; }
    public void setHasReceivedPayout(Boolean hasReceivedPayout) {
        this.hasReceivedPayout = hasReceivedPayout;
    }

    public LocalDateTime getJoinedAt() { return joinedAt; }
    public void setJoinedAt(LocalDateTime joinedAt) { this.joinedAt = joinedAt; }
}
