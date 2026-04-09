package com.application.stockfela.repository;

import com.application.stockfela.entity.SavingsGroup;
import com.application.stockfela.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for {@link SavingsGroup} entities.
 *
 * <p>The {@code @Repository} annotation enables Spring's persistence
 * exception translation (converts JPA exceptions into Spring's
 * {@link org.springframework.dao.DataAccessException} hierarchy).
 */
@Repository
public interface SavingsGroupRepository extends JpaRepository<SavingsGroup, Long> {

    /**
     * Find all groups that were created by the given user.
     *
     * @param createdBy the owning user entity
     * @return list of groups created by the user (may be empty)
     */
    List<SavingsGroup> findByCreatedBy(User createdBy);

    /**
     * Find all groups with the given status.
     *
     * @param status {@link SavingsGroup.GroupStatus#ACTIVE}, {@code COMPLETED}, or {@code CANCELLED}
     * @return list of matching groups
     */
    List<SavingsGroup> findByStatus(SavingsGroup.GroupStatus status);

    /**
     * Find all groups in which the given user holds a {@link com.application.stockfela.entity.GroupMember}
     * record. This covers both groups the user created and groups they were
     * added to later.
     *
     * @param userId the user's database ID
     * @return list of groups the user belongs to
     */
    @Query("SELECT sg FROM SavingsGroup sg JOIN sg.members gm WHERE gm.user.id = :userId")
    List<SavingsGroup> findGroupByMemberId(@Param("userId") Long userId);
}
