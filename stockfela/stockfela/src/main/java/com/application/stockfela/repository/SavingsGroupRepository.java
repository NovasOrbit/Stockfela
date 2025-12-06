package com.application.stockfela.repository;

import com.application.stockfela.entity.SavingsGroup;
import com.application.stockfela.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SavingsGroupRepository  extends JpaRepository<SavingsGroup, Long> {

    // Find all groups created by a specific user
    List<SavingsGroup> findByCreatedBy(User createdBy);

    // Find all active groups
    List<SavingsGroup> findByStatus(SavingsGroup.GroupStatus status);

    //Custom query to find groups where user is a member
    @Query("SELECT sg FROM SavingsGroup sg JOIN sg.members gm WHERE gm.user.id = :userId")
    List<SavingsGroup> findGroupByMemberId(@Param("userId") Long userId);
}
