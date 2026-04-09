package com.application.stockfela.controller;

import com.application.stockfela.dto.request.AddMemberRequest;
import com.application.stockfela.dto.request.CreateGroupRequest;
import com.application.stockfela.entity.SavingsGroup;
import com.application.stockfela.entity.User;
import com.application.stockfela.service.SavingGroupService;
import com.application.stockfela.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/groups")
public class GroupController {

    @Autowired
    private SavingGroupService savingGroupService;

    @Autowired
    private UserService userService;

    /**
     * CREATE A NEW SAVINGS GROUP
     * POST http://localhost:8080/api/groups
     * {
     *   "name": "Family Stock Fela",
     *   "description": "Monthly family contributions",
     *   "monthlyContribution": 1000.00,
     *   "cycleMonths": 6,
     *   "createdBy": 1
     * }
     */
    @PostMapping
    public ResponseEntity<?> createGroup(@RequestBody CreateGroupRequest request) {
        try {
            // Get the creator user from database
            User creator = userService.findById(request.getCreatedBy())
                    .orElseThrow(() -> new RuntimeException("User not found with ID: " + request.getCreatedBy()));

            // Create new group entity
            SavingsGroup group = new SavingsGroup();
            group.setName(request.getName());
            group.setDescription(request.getDescription());
            group.setMonthlyContribution(request.getMonthlyContribution());
            group.setCycleMonths(request.getCycleMonths());

            // Save the group
            SavingsGroup savedGroup = savingGroupService.createGroup(group, creator);

            // Return success response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Group created successfully");
            response.put("group", savedGroup);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            // Return error response
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * GET ALL GROUPS FOR A USER
     * GET http://localhost:8080/api/groups/user/1
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserGroups(@PathVariable Long userId) {
        try {
            List<SavingsGroup> groups = savingGroupService.getUserGroups(userId);

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
     * ADD MEMBER TO GROUP
     * POST http://localhost:8080/api/groups/1/members
     * {
     *   "userId": 2,
     *   "payoutOrder": 2
     * }
     */
    @PostMapping("/{groupId}/members")
    public ResponseEntity<?> addMemberToGroup(
            @PathVariable Long groupId,
            @RequestBody AddMemberRequest request) {
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
     * START NEW PAYOUT CYCLE
     * POST http://localhost:8080/api/groups/1/payout-cycle
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

    /**
     * GET GROUP DETAILS WITH MEMBERS
     * GET http://localhost:8080/api/groups/1
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
     * GET ALL MEMBERS OF A GROUP
     * GET http://localhost:8080/api/groups/1/members
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


}