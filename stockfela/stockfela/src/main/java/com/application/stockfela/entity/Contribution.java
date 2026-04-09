package com.application.stockfela.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * JPA entity representing a single member's contribution to a payout cycle.
 *
 * <p>When a new {@link PayoutCycle} is started,
 * {@link com.application.stockfela.service.PayoutCycleService#createContributionForPayoutCycle}
 * creates one {@code Contribution} record per group member with status
 * {@link ContributionStatus#PENDING}.
 *
 * <p>As each member pays, their record is updated to
 * {@link ContributionStatus#PAID}. Once all members have paid, the parent
 * {@link PayoutCycle} is automatically marked {@link PayoutCycle.PayoutStatus#COMPLETED}.
 *
 * <p>Mapped to the {@code contributions} table.
 */
@Entity
@Table(name = "contributions")
public class Contribution {

    // ── Primary key ──────────────────────────────────────────────────────────

    /** Auto-generated database primary key. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── Relationships ────────────────────────────────────────────────────────

    /** The savings group this contribution belongs to (denormalised for fast queries). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private SavingsGroup group;

    /** The member who is expected to (or has) made this payment. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** The payout cycle this contribution is part of. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payout_cycle_id", nullable = false)
    private PayoutCycle payoutCycle;

    // ── Contribution fields ──────────────────────────────────────────────────

    /**
     * The amount expected / paid for this contribution.
     * Initially set to the group's {@code monthlyContribution} and may be
     * updated with the actual amount when {@code recordPayment()} is called.
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    /**
     * The calendar date on which the payment was made.
     * {@code null} until the payment is recorded.
     */
    @Column(name = "payment_date")
    private LocalDate paymentDate;

    /**
     * Current payment status for this contribution.
     * Starts as {@link ContributionStatus#PENDING}.
     */
    @Enumerated(EnumType.STRING)
    private ContributionStatus status = ContributionStatus.PENDING;

    /**
     * Exact timestamp when the payment was recorded in the system.
     * More precise than {@link #paymentDate}.
     */
    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    // ── Audit ────────────────────────────────────────────────────────────────

    /** Timestamp set automatically when the record is first created. */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // ── Enum ─────────────────────────────────────────────────────────────────

    /**
     * Payment states for a contribution record.
     *
     * <ul>
     *   <li>{@code PENDING} – member has not yet paid.</li>
     *   <li>{@code PAID}    – payment confirmed and recorded.</li>
     *   <li>{@code OVERDUE} – payment deadline passed without payment.</li>
     * </ul>
     */
    public enum ContributionStatus {
        PENDING, PAID, OVERDUE
    }

    // ── Constructors ─────────────────────────────────────────────────────────

    /** Required by JPA. */
    public Contribution() {}

    /**
     * Convenience constructor used when bulk-creating contributions at the start
     * of a payout cycle.
     *
     * @param group       the savings group
     * @param user        the member responsible for this contribution
     * @param payoutCycle the cycle this contribution belongs to
     * @param amount      the expected contribution amount
     */
    public Contribution(SavingsGroup group, User user, PayoutCycle payoutCycle,
                        BigDecimal amount) {
        this.group = group;
        this.user = user;
        this.payoutCycle = payoutCycle;
        this.amount = amount;
    }

    // ── Getters and Setters ──────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public SavingsGroup getGroup() { return group; }
    public void setGroup(SavingsGroup group) { this.group = group; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public PayoutCycle getPayoutCycle() { return payoutCycle; }
    public void setPayoutCycle(PayoutCycle payoutCycle) { this.payoutCycle = payoutCycle; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public LocalDate getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDate paymentDate) { this.paymentDate = paymentDate; }

    public ContributionStatus getStatus() { return status; }
    public void setStatus(ContributionStatus status) { this.status = status; }

    public LocalDateTime getPaidAt() { return paidAt; }
    public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
