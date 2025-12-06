package com.application.stockfela.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "contributions")
public class Contribution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Many contributions belong to one group
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private SavingsGroup group;

    // Many contributions are made by one user
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Many contributions belong to one payout cycle
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payout_cycle_id", nullable = false)
    private PayoutCycle payoutCycle;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "payment_date")
    private LocalDate paymentDate;

    @Enumerated(EnumType.STRING)
    private ContributionStatus status = ContributionStatus.PENDING;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public enum ContributionStatus {
        PENDING,   // Payment not yet made
        PAID,      // Payment completed
        OVERDUE    // Payment overdue
    }

    // Constructors
    public Contribution() {}

    public Contribution(SavingsGroup group, User user, PayoutCycle payoutCycle,
                        BigDecimal amount) {
        this.group = group;
        this.user = user;
        this.payoutCycle = payoutCycle;
        this.amount = amount;
    }

    // Getters and Setters
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