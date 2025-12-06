package com.application.stockfela.entity;


import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "payout_cycles")
public class PayoutCycle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Many payout cycles belong to one group
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private SavingsGroup group;

    @Column(name = "cycle_number", nullable = false)
    private Integer cycleNumber; // Which month/cycle (1, 2, 3...)

    // The user who receives the payout this cycle
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_user_id", nullable = false)
    private User recipientUser;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "payout_date", nullable = false)
    private LocalDate payoutDate;

    @Enumerated(EnumType.STRING)
    private PayoutStatus status = PayoutStatus.PENDING;

    // One payout cycle can have multiple contributions
    @OneToMany(mappedBy = "payoutCycle", cascade = CascadeType.ALL)
    private List<Contribution> contributions;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public enum PayoutStatus {
        PENDING,    // Cycle started but not all payments received
        COMPLETED,  // All payments received and payout done
        FAILED      // Some payments missing or issues
    }

    // Constructors
    public PayoutCycle() {}

    public PayoutCycle(SavingsGroup group, Integer cycleNumber, User recipientUser,
                       BigDecimal totalAmount, LocalDate payoutDate) {
        this.group = group;
        this.cycleNumber = cycleNumber;
        this.recipientUser = recipientUser;
        this.totalAmount = totalAmount;
        this.payoutDate = payoutDate;
    }

    // Getters and Setters
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

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<Contribution> getContributions() { return contributions; }
    public void setContributions(List<Contribution> contributions) { this.contributions = contributions; }
}