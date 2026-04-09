package com.application.stockfela.repository;

import com.application.stockfela.entity.GroupMember;
import com.application.stockfela.entity.SavingsGroup;
import com.application.stockfela.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link GroupMember} join entities.
 *
 * <p>A {@link GroupMember} represents a single user's membership in a
 * single {@link SavingsGroup}, including their payout order and whether
 * they have already received their cycle payout.
 */
@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {

    /**
     * Find all members belonging to the given group.
     *
     * @param group the savings group to query
     * @return list of group member records (may be empty)
     */
    List<GroupMember> findByGroup(SavingsGroup group);

    /**
     * Find the membership record for a specific user in a specific group.
     * Used to check whether a user is already a member before adding them.
     *
     * @param group the savings group
     * @param user  the candidate member
     * @return an {@link Optional} with the record if found
     */
    Optional<GroupMember> findByGroupAndUser(SavingsGroup group, User user);

    /**
     * Find the next member(s) eligible to receive the group payout.
     * Returns members who have <strong>not yet</strong> received a payout,
     * ordered ascending by {@code payoutOrder} so the lowest number
     * (first in line) is returned first.
     *
     * @param groupId the group's database ID
     * @return ordered list of eligible recipients; the first element is next
     */
    @Query("SELECT gm FROM GroupMember gm " +
           "WHERE gm.group.id = :groupId " +
           "AND gm.hasReceivedPayout = false " +
           "ORDER BY gm.payoutOrder ASC")
    List<GroupMember> findNextPayoutRecipient(@Param("groupId") Long groupId);

    /**
     * Count the total number of members in a group.
     * Used when calculating the total payout amount for a cycle.
     *
     * @param group the savings group
     * @return number of members (including the creator)
     */
    Long countByGroup(SavingsGroup group);
}
