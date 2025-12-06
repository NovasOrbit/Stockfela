package com.application.stockfela.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "savings_groups")
public class SavingsGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    private String description;

    @Column(name = "monthly_contribution", nullable = false, precision = 10, scale = 2)
    private BigDecimal monthlyContribution;

    @Column(name = "cycle_months", nullable = false)
    private Integer cycleMonths;

    @Column(name = "current_cycle")
    private Integer currentCycle = 1;

    @Enumerated(EnumType.STRING)
    private GroupStatus status = GroupStatus.ACTIVE;

    // Many groups can be created by one user
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    // One group can have multiple members
    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL)
    private List<GroupMember> members;

    // One group can have multiple payout cycles
    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL)
    private List<PayoutCycle> payoutCycles;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public enum GroupStatus {
        ACTIVE, COMPLETED, CANCELLED
    }

    // Constructors, Getters and Setters
    public SavingsGroup() {}

    public SavingsGroup(String name, String description, BigDecimal monthlyContribution,
                        Integer cycleMonths, User createdBy) {
        this.name = name;
        this.description = description;
        this.monthlyContribution = monthlyContribution;
        this.cycleMonths = cycleMonths;
        this.createdBy = createdBy;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getMonthlyContribution() { return monthlyContribution; }
    public void setMonthlyContribution(BigDecimal monthlyContribution) { this.monthlyContribution = monthlyContribution; }

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
    public void setPayoutCycles(List<PayoutCycle> payoutCycles) { this.payoutCycles = payoutCycles; }
}