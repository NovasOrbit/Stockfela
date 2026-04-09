package com.application.stockfela.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * JPA entity representing one payout cycle within a savings group.
 *
 * <p>A payout cycle corresponds to a single month in the ROSCA rotation:
 * <ol>
 *   <li>One member is designated as the {@link #recipientUser} for this cycle.</li>
 *   <li>Every other member must contribute their fixed monthly amount.</li>
 *   <li>The pooled total is paid out to the recipient.</li>
 *   <li>The cycle moves to {@link PayoutStatus#COMPLETED} once all
 *       {@link Contribution} records are marked {@code PAID}.</li>
 * </ol>
 *
 * <p>Mapped to the {@code payout_cycles} table.
 */
@Entity
@Table(name = "payout_cycles")
public class PayoutCycle {

    // ── Primary key ──────────────────────────────────────────────────────────

    /** Auto-generated database primary key. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── Relationships ────────────────────────────────────────────────────────

    /**
     * The savings group this cycle belongs to.
     * Loaded lazily to avoid fetching the full group graph on every cycle query.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private SavingsGroup group;

    /**
     * The user who will receive the pooled payout at the end of this cycle.
     * Determined by their {@link GroupMember#getPayoutOrder()} value.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_user_id", nullable = false)
    private User recipientUser;

    /**
     * All individual contribution records associated with this cycle –
     * one per group member.
     */
    @OneToMany(mappedBy = "payoutCycle", cascade = CascadeType.ALL)
    private List<Contribution> contributions;

    // ── Cycle fields ─────────────────────────────────────────────────────────

    /**
     * The sequential cycle number (1 = first cycle, 2 = second, etc.).
     * Matches the {@link SavingsGroup#getCurrentCycle()} value at the time
     * the cycle was created.
     */
    @Column(name = "cycle_number", nullable = false)
    private Integer cycleNumber;

    /**
     * Total amount expected to be collected for this cycle.
     * Calculated as: {@code numberOfMembers × monthlyContribution}.
     */
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    /**
     * The target date by which all contributions should be paid and the
     * recipient should receive the payout.
     */
    @Column(name = "payout_date", nullable = false)
    private LocalDate payoutDate;

    /**
     * Current status of this cycle.
     * Starts as {@link PayoutStatus#PENDING} and advances to
     * {@link PayoutStatus#COMPLETED} automatically once all members have paid.
     */
    @Enumerated(EnumType.STRING)
    private PayoutStatus status = PayoutStatus.PENDING;

    // ── Audit ────────────────────────────────────────────────────────────────

    /** Timestamp set automatically when the cycle record is first created. */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // ── Enum ─────────────────────────────────────────────────────────────────

    /**
     * Lifecycle states for a payout cycle.
     *
     * <ul>
     *   <li>{@code PENDING}   – cycle started; waiting for contributions.</li>
     *   <li>{@code COMPLETED} – all contributions received and payout disbursed.</li>
     *   <li>{@code FAILED}    – cycle ended with outstanding contributions.</li>
     * </ul>
     */
    public enum PayoutStatus {
        PENDING, COMPLETED, FAILED
    }

    // ── Constructors ─────────────────────────────────────────────────────────

    /** Required by JPA. */
    public PayoutCycle() {}

    /**
     * Convenience constructor for programmatic creation.
     *
     * @param group         the savings group
     * @param cycleNumber   sequential cycle number
     * @param recipientUser the member who receives the payout this cycle
     * @param totalAmount   expected total collection for this cycle
     * @param payoutDate    target date for disbursement
     */
    public PayoutCycle(SavingsGroup group, Integer cycleNumber, User recipientUser,
                       BigDecimal totalAmount, LocalDate payoutDate) {
        this.group = group;
        this.cycleNumber = cycleNumber;
        this.recipientUser = recipientUser;
        this.totalAmount = totalAmount;
        this.payoutDate = payoutDate;
    }

    // ── Getters and Setters ──────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public SavingsGroup getGroup() { return group; }
    public void setGroup(SavingsGroup group) { this.group = group; }

    public Integer getCycleNumber() { return cycleNumber; }
    public void setCycleNumber(Integer cycleNumber) { this.cycleNumber = cycleNumber; }

    public User getRecipientUser() { return recipientUser; }
    public void setRecipientUser(User recipientUser) { this.recipientUser = recipientUser; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public LocalDate getPayoutDate() { return payoutDate; }
    public void setPayoutDate(LocalDate payoutDate) { this.payoutDate = payoutDate; }

    public PayoutStatus getStatus() { return status; }
    public void setStatus(PayoutStatus status) { this.status = status; }

    public List<Contribution> getContributions() { return contributions; }
    public void setContributions(List<Contribution> contributions) {
        this.contributions = contributions;
    }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
