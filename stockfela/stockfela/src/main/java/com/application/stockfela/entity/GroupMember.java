package com.application.stockfela.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "group_members")
public class GroupMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Many group members belong to one group
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private SavingsGroup group;

    // Many group members are users
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "payout_order", nullable = false)
    private Integer payoutOrder;

    @Column(name = "has_received_payout")
    private Boolean hasReceivedPayout = false;

    @CreationTimestamp
    @Column(name = "joined_at", updatable = false)
    private LocalDateTime joinedAt;

    // Constructors
    public GroupMember() {}

    public GroupMember(SavingsGroup group, User user, Integer payoutOrder) {
        this.group = group;
        this.user = user;
        this.payoutOrder = payoutOrder;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public SavingsGroup getGroup() { return group; }
    public void setGroup(SavingsGroup group) { this.group = group; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Integer getPayoutOrder() { return payoutOrder; }
    public void setPayoutOrder(Integer payoutOrder) { this.payoutOrder = payoutOrder; }

    public Boolean getHasReceivedPayout() { return hasReceivedPayout; }
    public void setHasReceivedPayout(Boolean hasReceivedPayout) { this.hasReceivedPayout = hasReceivedPayout; }

    public LocalDateTime getJoinedAt() { return joinedAt; }
    public void setJoinedAt(LocalDateTime joinedAt) { this.joinedAt = joinedAt; }
}