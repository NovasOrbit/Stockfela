package com.application.stockfela.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * Request body for adding a member to an existing savings group.
 *
 * <pre>POST /api/groups/{groupId}/members</pre>
 *
 * <p>Example JSON:
 * <pre>{@code { "userId": 2, "payoutOrder": 2 }}</pre>
 */
@Data
@Builder
@AllArgsConstructor
public class AddMemberRequest {

    /** Database ID of the user to add to the group. */
    @NotNull(message = "User ID is required")
    private Long userId;

    /**
     * 1-based position in the payout queue.
     * The member with order 1 receives the payout first, order 2 second, etc.
     * Must be unique within the group.
     */
    @NotNull(message = "Payout order is required")
    @Min(value = 1, message = "Payout order must be at least 1")
    private Integer payoutOrder;
}
