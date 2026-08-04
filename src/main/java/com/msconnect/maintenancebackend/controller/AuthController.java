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

    public AuthController(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            String cleanEmail = (request.getEmail() != null) 
                    ? request.getEmail().trim().toLowerCase() 
                    : "";

            if (cleanEmail.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Email is required!"));
            }

            if (userRepository.existsByEmailIgnoreCase(cleanEmail)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Email already registered!"));
            }

            String userRole = (request.getRole() != null && !request.getRole().isEmpty()) 
                    ? request.getRole().trim().toLowerCase() 
                    : "student";

            User user = new User();
            user.setName(request.getName());
            user.setEmail(cleanEmail);
            user.setRole(userRole);
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
            
            user.setHostel(request.getHostel());
            user.setRoomNo(request.getRoomNo());
            user.setLatitude(request.getLatitude());
            user.setLongitude(request.getLongitude());

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
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        if (request == null || request.getEmail() == null || request.getPassword() == null) {
            return ResponseEntity.status(400).body(Map.of("error", "Email and password are required."));
        }

        String cleanEmail = request.getEmail().trim().toLowerCase();
        User user = userRepository.findByEmailIgnoreCase(cleanEmail).orElse(null);

        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid email or password"));
        }

        boolean passwordMatches = passwordEncoder.matches(request.getPassword(), user.getPasswordHash());
        
        if (!passwordMatches && request.getPassword().equals(user.getPasswordHash())) {
            passwordMatches = true;
        }

        if (!passwordMatches) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid email or password"));
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole());

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

    // ✅ ADMIN-ONLY: Create new admin account
    @PostMapping("/admin/create")
public ResponseEntity<?> createAdmin(@RequestBody RegisterRequest request, @RequestHeader("Authorization") String authHeader) {
    System.out.println("🔍 ===== CREATE ADMIN REQUEST =====");
    System.out.println("🔍 Auth Header: " + (authHeader != null ? authHeader.substring(0, Math.min(authHeader.length(), 50)) + "..." : "null"));
    
    try {
        // 1. Check if Authorization header exists
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("❌ No Bearer token found!");
            return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
        }
        
        // 2. Extract token and validate
        String token = authHeader.substring(7);
        System.out.println("🔍 Token: " + token.substring(0, Math.min(token.length(), 30)) + "...");
        
        // 3. Extract email and role from token
        String requesterEmail = jwtUtil.extractEmail(token);
        String requesterRole = jwtUtil.extractRole(token);
        
        System.out.println("🔍 Email from token: " + requesterEmail);
        System.out.println("🔍 Role from token: " + requesterRole);
        
        // 4. Check if user exists and is admin
        User requester = userRepository.findByEmailIgnoreCase(requesterEmail).orElse(null);
        if (requester == null) {
            System.out.println("❌ User not found in database!");
            return ResponseEntity.status(403).body(Map.of("error", "User not found"));
        }
        
        System.out.println("🔍 Database role: " + requester.getRole());
        System.out.println("🔍 Database ID: " + requester.getId());
        
        if (!"admin".equalsIgnoreCase(requester.getRole())) {
            System.out.println("❌ User is NOT an admin! Role: " + requester.getRole());
            return ResponseEntity.status(403).body(Map.of("error", "Only admins can create new admin accounts"));
        }
        
        System.out.println("✅ User IS an admin! Proceeding...");
        
        // 5. Validate new admin's email
        String cleanEmail = request.getEmail().trim().toLowerCase();
        if (cleanEmail.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email is required"));
        }
        
        if (userRepository.existsByEmailIgnoreCase(cleanEmail)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email already registered"));
        }
        
        // 6. Create the new admin
        User newAdmin = new User();
        newAdmin.setName(request.getName());
        newAdmin.setEmail(cleanEmail);
        newAdmin.setRole("admin");
        newAdmin.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        newAdmin.setIsAvailable(false);
        
        userRepository.save(newAdmin);
        
        System.out.println("✅ Admin created successfully! ID: " + newAdmin.getId());
        
        return ResponseEntity.ok(Map.of(
            "message", "Admin account created successfully!",
            "id", newAdmin.getId(),
            "email", newAdmin.getEmail(),
            "role", newAdmin.getRole()
        ));
        
    } catch (Exception e) {
        System.out.println("❌ Exception: " + e.getMessage());
        e.printStackTrace();
        return ResponseEntity.status(500).body(Map.of("error", "Server error: " + e.getMessage()));
    }
    }
}