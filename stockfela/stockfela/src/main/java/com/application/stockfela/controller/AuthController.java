package com.application.stockfela.controller;

import com.application.stockfela.dto.request.LoginRequest;
import com.application.stockfela.entity.User;
import com.application.stockfela.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UserService userService;

    /**
     * Register a new user
     * POST http://localhost:8080/api/auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody User user) {
        try {
            User savedUser = userService.registerUser(user);

            // Return user without password
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "User registered successfully");
            response.put("user", Map.of(
                    "id", savedUser.getId(),
                    "username", savedUser.getUsername(),
                    "email", savedUser.getEmail(),
                    "fullName", savedUser.getFullName()
            ));

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * User login (basic version - we'll add JWT later)
     * POST http://localhost:8080/api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest loginRequest) {
        try {
            // For now, simple authentication. We'll add JWT later
            User user = userService.findByUsername(loginRequest.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // In real app, we'd use PasswordEncoder to check password
            // For now, we'll just return success

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Login successful");
            response.put("user", Map.of(
                    "id", user.getId(),
                    "username", user.getUsername(),
                    "email", user.getEmail(),
                    "fullName", user.getFullName()
            ));

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Get all users (for testing)
     * GET http://localhost:8080/api/auth/users
     */
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        try {
            var users = userService.getAllUsers();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("users", users);
            response.put("count", users.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // Inner class for login request
//    public static class LoginRequest {
//        private String username;
//        private String password;
//
//        // Getters and Setters
//        public String getUsername() { return username; }
//        public void setUsername(String username) { this.username = username; }
//
//        public String getPassword() { return password; }
//        public void setPassword(String password) { this.password = password; }
//    }
}