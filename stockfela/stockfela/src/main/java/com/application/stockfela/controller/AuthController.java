package com.application.stockfela.controller;

import com.application.stockfela.JWT.JWTUtilities;
import com.application.stockfela.dto.request.LoginRequest;
import com.application.stockfela.dto.request.PaymentRequest;
import com.application.stockfela.dto.request.RegisterRequest;
import com.application.stockfela.dto.response.LoginResponse;
import com.application.stockfela.dto.response.RegisterResponse;
import com.application.stockfela.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JWTUtilities jwtUtilities;

    public AuthController(UserService userService){
        this.userService = userService;
    }

    /**
     * Register a new user
     * POST http://localhost:8080/api/auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        RegisterResponse response = userService.registerUser(registerRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);

   }

    /**
     * User login (basic version - we'll add JWT later)
     * POST http://localhost:8080/api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest loginRequest) {
        Authentication authentication;
        try {

            // For now, simple authentication. We'll add JWT later
//            User user = userService.findByUsername(loginRequest.getUsername())
//                    .orElseThrow(() -> new RuntimeException("User not found"));

            // In real app, we'd use PasswordEncoder to check password
            // For now, we'll just return success
//
//            Map<String, Object> response = new HashMap<>();
//            response.put("success", true);
//            response.put("message", "Login successful");
//            response.put("user", Map.of(
//                    "id", user.getId(),
//                    "username", user.getUsername(),
//                    "email", user.getEmail(),
//                    "fullName", user.getFullName()
            authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(),loginRequest.getPassword()));
//            ));
//
//            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetails userDetails =(UserDetails) authentication.getPrincipal();

        String jwtToken = jwtUtilities.generateTokenFromUsername(userDetails);
//we dont have roles yet
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).collect(Collectors.toList());

        LoginResponse response = new LoginResponse(jwtToken,userDetails.getUsername(),roles);

        return ResponseEntity.ok(response);
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

}