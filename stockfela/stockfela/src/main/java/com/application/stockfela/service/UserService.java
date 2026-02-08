package com.application.stockfela.service; // FIXED PACKAGE NAME

import com.application.stockfela.dto.StockfelaMapper;
import com.application.stockfela.dto.request.RegisterRequest;
import com.application.stockfela.dto.response.LoginResponse;
import com.application.stockfela.dto.response.RegisterResponse;
import com.application.stockfela.entity.User;
import com.application.stockfela.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;

import static com.application.stockfela.dto.StockfelaMapper.mapToRegisterResponse;

@Service
public class UserService {

    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

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

        //3) Encode password (defensive check, though @Valid already guards this)
        if (registerRequest.getPassword() ==  null || registerRequest.getPassword().isBlank())
        {
            throw  new IllegalArgumentException("Password cannot be null or blank");
        }
        // Encrypt the password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));

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
}