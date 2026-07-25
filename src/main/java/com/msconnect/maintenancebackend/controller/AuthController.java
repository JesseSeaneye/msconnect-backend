package com.msconnect.maintenancebackend.controller;

import com.msconnect.maintenancebackend.dto.AuthRequest;
import com.msconnect.maintenancebackend.dto.AuthResponse;
import com.msconnect.maintenancebackend.dto.RegisterRequest;
import com.msconnect.maintenancebackend.entity.User;
import com.msconnect.maintenancebackend.repository.UserRepository;
import com.msconnect.maintenancebackend.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;

    // Constructor Injection
    public AuthController(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            // Clean and normalize email input
            String cleanEmail = (request.getEmail() != null) 
                    ? request.getEmail().trim().toLowerCase() 
                    : "";

            if (cleanEmail.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Email is required!"));
            }

            // Check if user exists (case-insensitive check)
            if (userRepository.existsByEmailIgnoreCase(cleanEmail)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Email already registered!"));
            }

            // Determine role (default to "student" if null/empty)
            String userRole = (request.getRole() != null && !request.getRole().isEmpty()) 
                    ? request.getRole().trim().toLowerCase() 
                    : "student";

            // Create new user entity
            User user = new User();
            user.setName(request.getName());
            user.setEmail(cleanEmail);
            user.setRole(userRole);
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
            
            // Campus location / residence fields
            user.setHostel(request.getHostel());
            user.setRoomNo(request.getRoomNo());
            user.setLatitude(request.getLatitude());
            user.setLongitude(request.getLongitude());

            // Technician specific fields vs Student/Admin safety
            if ("technician".equalsIgnoreCase(userRole)) {
                user.setSpecialty(request.getSpecialty());
                user.setIsAvailable(request.getIsAvailable() != null ? request.getIsAvailable() : true);
            } else {
                user.setSpecialty(null);
                user.setIsAvailable(false);
            }

            userRepository.save(user);

            return ResponseEntity.ok(Map.of(
                "message", "Registration successful!",
                "userId", user.getId(),
                "id", user.getId(),
                "role", user.getRole()
            ));
        } catch (Exception e) {
            e.printStackTrace(); // Logs error details to terminal
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        // Safety check for null request or null email/password
        if (request == null || request.getEmail() == null || request.getPassword() == null) {
            return ResponseEntity.status(400).body(Map.of("error", "Email and password are required."));
        }

        // Clean and normalize email input
        String cleanEmail = request.getEmail().trim().toLowerCase();

        // Find user using CASE-INSENSITIVE lookup
        User user = userRepository.findByEmailIgnoreCase(cleanEmail).orElse(null);

        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid email or password"));
        }

        // Verify password (supports BCrypt & plaintext fallback for old DB entries)
        boolean passwordMatches = passwordEncoder.matches(request.getPassword(), user.getPasswordHash());
        
        if (!passwordMatches && request.getPassword().equals(user.getPasswordHash())) {
            passwordMatches = true;
        }

        if (!passwordMatches) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid email or password"));
        }

        // Generate JWT token
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole());

        // Return structured payload matching React Native expectations
        return ResponseEntity.ok(Map.of(
            "token", token,
            "role", user.getRole() != null ? user.getRole().toLowerCase() : "student",
            "name", user.getName() != null ? user.getName() : "",
            "id", user.getId(),
            "userId", user.getId()
        ));
    }

    @GetMapping("/test")
    public ResponseEntity<?> test() {
        return ResponseEntity.ok(Map.of("message", "Auth endpoints are operational!"));
    }
}