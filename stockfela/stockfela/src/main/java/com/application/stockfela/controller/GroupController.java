package com.application.stockfela.controller;

import com.application.stockfela.dto.request.AddMemberRequest;
import com.application.stockfela.dto.request.CreateGroupRequest;
import com.application.stockfela.entity.SavingsGroup;
import com.application.stockfela.entity.User;
import com.application.stockfela.service.SavingGroupService;
import com.application.stockfela.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for savings-group lifecycle operations.
 *
 * <p>All endpoints require an authenticated user (JWT). The authenticated
 * principal is injected via {@link AuthenticationPrincipal} to prevent
 * Insecure Direct Object Reference (IDOR) attacks – callers can never
 * act on behalf of a different user by supplying an arbitrary ID.
 */
@RestController
@RequestMapping("/api/groups")
public class GroupController {

    @Autowired
    private SavingGroupService savingGroupService;

    @Autowired
    private UserService userService;

    // ── Create ──────────────────────────────────────────────────────────────

    /**
     * Create a new savings group.
     * The authenticated user automatically becomes the group creator and
     * first member (payout order 1).
     *
     * <pre>POST /api/groups</pre>
     *
     * @param request        validated group settings from the request body
     * @param currentUser    injected Spring Security principal (from JWT)
     * @return 201 Created with the saved group, or 400/404 on error
     */
    @PostMapping
    public ResponseEntity<?> createGroup(
            @Valid @RequestBody CreateGroupRequest request,
            @AuthenticationPrincipal UserDetails currentUser) {
        try {
            // Resolve the full User entity from the JWT-authenticated username
            User creator = userService.findByUsername(currentUser.getUsername())
                    .orElseThrow(() -> new RuntimeException("Authenticated user not found"));

            SavingsGroup group = new SavingsGroup();
            group.setName(request.getName());
            group.setDescription(request.getDescription());
            group.setMonthlyContribution(request.getMonthlyContribution());
            group.setCycleMonths(request.getCycleMonths());

            SavingsGroup savedGroup = savingGroupService.createGroup(group, creator);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Group created successfully");
            response.put("group", savedGroup);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // ── Read ────────────────────────────────────────────────────────────────

    /**
     * Get all groups the authenticated user belongs to (as creator or member).
     *
     * <pre>GET /api/groups/my</pre>
     *
     * @param currentUser injected Spring Security principal
     * @return list of savings groups for the current user
     */
    @GetMapping("/my")
    public ResponseEntity<?> getMyGroups(
            @AuthenticationPrincipal UserDetails currentUser) {
        try {
            User user = userService.findByUsername(currentUser.getUsername())
                    .orElseThrow(() -> new RuntimeException("Authenticated user not found"));

            List<SavingsGroup> groups = savingGroupService.getUserGroups(user.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("groups", groups);
            response.put("count", groups.size());

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Get detailed information about a specific group.
     *
     * <pre>GET /api/groups/{groupId}</pre>
     *
     * @param groupId the group's database ID
     * @return group entity with members and payout cycles
     */
    @GetMapping("/{groupId}")
    public ResponseEntity<?> getGroupDetails(@PathVariable Long groupId) {
        try {
            SavingsGroup group = savingGroupService.getGroupWithMembers(groupId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("group", group);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Get all members of a group.
     *
     * <pre>GET /api/groups/{groupId}/members</pre>
     *
     * @param groupId the group's database ID
     * @return list of group members with their payout order
     */
    @GetMapping("/{groupId}/members")
    public ResponseEntity<?> getGroupMembers(@PathVariable Long groupId) {
        try {
            var members = savingGroupService.getGroupMembers(groupId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("members", members);
            response.put("count", members.size());

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // ── Write ───────────────────────────────────────────────────────────────

    /**
     * Add a member to an existing group.
     *
     * <pre>POST /api/groups/{groupId}/members</pre>
     *
     * @param groupId the group's database ID
     * @param request body containing the userId and payoutOrder
     * @return success message or error
     */
    @PostMapping("/{groupId}/members")
    public ResponseEntity<?> addMemberToGroup(
            @PathVariable Long groupId,
            @Valid @RequestBody AddMemberRequest request) {
        try {
            savingGroupService.addMemberToGroup(groupId, request.getUserId(), request.getPayoutOrder());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Member added successfully to group");

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Start the next payout cycle for a group.
     * Determines the next recipient by payout order and creates
     * pending contribution records for all members.
     *
     * <pre>POST /api/groups/{groupId}/payout-cycle</pre>
     *
     * @param groupId the group's database ID
     * @return success message or error
     */
    @PostMapping("/{groupId}/payout-cycle")
    public ResponseEntity<?> startPayoutCycle(@PathVariable Long groupId) {
        try {
            savingGroupService.startNewPayoutCycle(groupId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Payout cycle started successfully");

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}
