package com.msconnect.maintenancebackend.dto;

public class AuthResponse {
    private String token;
    private String role;
    private String name;
    private Long userId;

    // Constructor with all fields
    public AuthResponse(String token, String role, String name, Long userId) {
        this.token = token;
        this.role = role;
        this.name = name;
        this.userId = userId;
    }

    // Getters
    public String getToken() { return token; }
    public String getRole() { return role; }
    public String getName() { return name; }
    public Long getUserId() { return userId; }

    // Setters
    public void setToken(String token) { this.token = token; }
    public void setRole(String role) { this.role = role; }
    public void setName(String name) { this.name = name; }
    public void setUserId(Long userId) { this.userId = userId; }
}