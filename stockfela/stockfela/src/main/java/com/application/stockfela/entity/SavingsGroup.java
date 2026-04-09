package com.application.stockfela.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * JPA entity representing a ROSCA savings group.
 *
 * <p>In a ROSCA (Rotating Savings and Credit Association), also known as a
 * "stokvel" in South African culture:
 * <ol>
 *   <li>A fixed number of members each commit to contributing a set amount
 *       every month ({@link #monthlyContribution}).</li>
 *   <li>Each month, the entire pooled amount is paid out to one member in a
 *       pre-agreed order ({@link #payoutCycles}).</li>
 *   <li>The rotation continues until every member has received the payout
 *       exactly once ({@link #cycleMonths} total cycles).</li>
 * </ol>
 *
 * <p>Mapped to the {@code savings_groups} table.
 */
@Entity
@Table(name = "savings_groups")
public class SavingsGroup {

    // ── Primary key ──────────────────────────────────────────────────────────

    /** Auto-generated database primary key. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── Core fields ──────────────────────────────────────────────────────────

    /** Human-readable name for the group (max 100 characters). */
    @Column(nullable = false, length = 100)
    private String name;

    /** Optional free-text description of the group's purpose. */
    private String description;

    /**
     * Fixed amount each member must contribute per cycle.
     * Stored with up to 10 digits of precision and 2 decimal places.
     */
    @Column(name = "monthly_contribution", nullable = false, precision = 10, scale = 2)
    private BigDecimal monthlyContribution;

    /**
     * Total number of payout cycles (= number of members that will eventually
     * receive the payout = number of months the group runs).
     */
    @Column(name = "cycle_months", nullable = false)
    private Integer cycleMonths;

    /**
     * The cycle number currently in progress.
     * Starts at 1 and increments after each payout cycle is started.
     * When {@code currentCycle > cycleMonths} the group is completed.
     */
    @Column(name = "current_cycle")
    private Integer currentCycle = 1;

    /**
     * Lifecycle status of the group.
     * Stored as a string in the database for readability.
     */
    @Enumerated(EnumType.STRING)
    private GroupStatus status = GroupStatus.ACTIVE;

    // ── Relationships ────────────────────────────────────────────────────────

    /**
     * The user who created this group and holds administrative rights.
     * {@code @JsonIgnoreProperties} prevents circular serialisation:
     * User → createdGroups → User.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    @JsonIgnoreProperties({"createdGroups", "groupMemberships", "password"})
    private User createdBy;

    /**
     * All members of this group, including the creator.
     * {@code @JsonIgnoreProperties} stops the Member → Group → Member loop.
     */
    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL)
    @JsonIgnoreProperties("group")
    private List<GroupMember> members;

    /**
     * Historical and active payout cycles for this group.
     * One per rotation (month).
     */
    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL)
    @JsonIgnoreProperties("group")
    private List<PayoutCycle> payoutCycles;

    // ── Audit ────────────────────────────────────────────────────────────────

    /** Timestamp set automatically when the group is first created. */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // ── Enum ─────────────────────────────────────────────────────────────────

    /**
     * Lifecycle states for a savings group.
     *
     * <ul>
     *   <li>{@code ACTIVE}    – accepting contributions and running cycles.</li>
     *   <li>{@code COMPLETED} – all members have received their payout.</li>
     *   <li>{@code CANCELLED} – disbanded before all cycles completed.</li>
     * </ul>
     */
    public enum GroupStatus {
        ACTIVE, COMPLETED, CANCELLED
    }

    // ── Constructors ─────────────────────────────────────────────────────────

    /** Required by JPA. */
    public SavingsGroup() {}

    /**
     * Full constructor for programmatic creation.
     *
     * @param name                human-readable group name
     * @param description         optional description
     * @param monthlyContribution fixed contribution per member per cycle
     * @param cycleMonths         number of cycles (= number of members)
     * @param createdBy           the founding user
     */
    public SavingsGroup(String name, String description,
                        BigDecimal monthlyContribution,
                        Integer cycleMonths, User createdBy) {
        this.name = name;
        this.description = description;
        this.monthlyContribution = monthlyContribution;
        this.cycleMonths = cycleMonths;
        this.createdBy = createdBy;
    }

    // ── Getters and Setters ──────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getMonthlyContribution() { return monthlyContribution; }
    public void setMonthlyContribution(BigDecimal monthlyContribution) {
        this.monthlyContribution = monthlyContribution;
    }

    public Integer getCycleMonths() { return cycleMonths; }
    public void setCycleMonths(Integer cycleMonths) { this.cycleMonths = cycleMonths; }

    public Integer getCurrentCycle() { return currentCycle; }
    public void setCurrentCycle(Integer currentCycle) { this.currentCycle = currentCycle; }

    public GroupStatus getStatus() { return status; }
    public void setStatus(GroupStatus status) { this.status = status; }

    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<GroupMember> getMembers() { return members; }
    public void setMembers(List<GroupMember> members) { this.members = members; }

    public List<PayoutCycle> getPayoutCycles() { return payoutCycles; }
    public void setPayoutCycles(List<PayoutCycle> payoutCycles) {
        this.payoutCycles = payoutCycles;
    }
}
