package com.application.stockfela.repository;

import com.application.stockfela.entity.GroupMember;
import com.application.stockfela.entity.SavingsGroup;
import com.application.stockfela.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {

    List<GroupMember> findByGroup(SavingsGroup group);

    Optional<GroupMember> findByGroupAndUser(SavingsGroup group, User user);

    @Query("SELECT gm FROM GroupMember gm WHERE gm.group.id = :groupId AND gm.hasReceivedPayout = false ORDER BY gm.payoutOrder ASC")
    List<GroupMember> findNextPayoutRecipient(@Param("groupId") Long groupId);

    Long countByGroup(SavingsGroup group);
}