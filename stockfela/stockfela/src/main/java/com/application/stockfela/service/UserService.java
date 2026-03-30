package com.application.stockfela.service; // FIXED PACKAGE NAME

import com.application.stockfela.dto.request.RegisterRequest;
import com.application.stockfela.dto.response.RegisterResponse;
import com.application.stockfela.entity.Role;
import com.application.stockfela.entity.User;
import com.application.stockfela.repository.RoleRepository;
import com.application.stockfela.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.*;
import java.util.stream.Collectors;

import static com.application.stockfela.dto.StockfelaMapper.mapToRegisterResponse;

@Service
@RequiredArgsConstructor

public class UserService implements UserDetailsService {

    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private final PasswordEncoder passwordEncoder;


    private final RoleRepository roleRepository;

    /**
     * Register a new user with encrypted password
     */
    public RegisterResponse registerUser(RegisterRequest registerRequest) {

        // 1) Validate duplicates against the *request*, not the empty User
        // Check if username or email already exists
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new RuntimeException("Email already exists");
        }


        // 2) Map DTO -> Entity
        User user = new User();
        user.setUsername(registerRequest.getUsername().trim());
        user.setEmail(registerRequest.getEmail().trim().toLowerCase());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setFullName(registerRequest.getFullName().trim());
        user.setPhoneNumber(registerRequest.getPhoneNumber());


        // Assign roles
        Set<Role> roles = registerRequest.getRoles().stream()
                .map(Role.RoleName::valueOf) // convert String → Enum
                .map(roleName -> roleRepository.findByName(roleName)
                        .orElseThrow(() -> new IllegalStateException("Role not seeded: " + roleName)))
                .collect(Collectors.toSet());

        user.setRoles(roles);



        //3) Encode password (defensive check, though @Valid already guards this)
        if (registerRequest.getPassword() ==  null || registerRequest.getPassword().isBlank())
        {
            throw  new IllegalArgumentException("Password cannot be null or blank");
        }

        User saved = userRepository.save(user);

        return mapToRegisterResponse(saved);
    }

    /**
     * Find user by username
     */
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Get user by ID
     */
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * Update user profile
     */
    public User updateUser(User user) {
        return userRepository.save(user);
    }

    /**
     * Get all users (for testing/administration)
     */
    public java.util.List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        List<SimpleGrantedAuthority> authorities =
                user.getRoles().stream()
                        .map(role -> new SimpleGrantedAuthority(role.getName().name()))
                        .toList();

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                authorities
        );
    }

}