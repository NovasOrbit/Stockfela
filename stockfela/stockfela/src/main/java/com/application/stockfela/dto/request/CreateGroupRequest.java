package com.application.stockfela.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Request body for creating a new savings group.
 *
 * <p>The group creator is <strong>not</strong> supplied here; it is derived
 * from the authenticated principal in the security context to prevent IDOR
 * attacks where a caller could create a group on behalf of another user.
 */
@Data
public class CreateGroupRequest {

    /** Human-readable name for the savings group (required). */
    @NotBlank(message = "Group name is required")
    private String name;

    /** Optional free-text description of the group's purpose. */
    private String description;

    /** Fixed monthly contribution amount each member must pay (ZAR). */
    @NotNull(message = "Monthly contribution amount is required")
    @DecimalMin(value = "1.00", message = "Monthly contribution must be at least 1.00")
    private BigDecimal monthlyContribution;

    /** Total number of months (= number of payout cycles) for this group. */
    @NotNull(message = "Cycle months is required")
    @Min(value = 2, message = "A group must have at least 2 cycles")
    @Max(value = 60, message = "A group cannot exceed 60 cycles")
    private Integer cycleMonths;
}
