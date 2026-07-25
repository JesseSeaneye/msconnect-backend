package com.msconnect.maintenancebackend.dto;

public class RegisterRequest {

    private String name;
    private String email;
    private String password;
    private String role;
    private String hostel;
    private String roomNo;
    private Double latitude;
    private Double longitude;
    private String specialty;
    private Boolean isAvailable;

    // --- Getters ---
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getRole() { return role; }
    public String getHostel() { return hostel; }
    public String getRoomNo() { return roomNo; }
    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }
    public String getSpecialty() { return specialty; }
    public Boolean getIsAvailable() { return isAvailable; }

    // --- Setters ---
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setRole(String role) { this.role = role; }
    public void setHostel(String hostel) { this.hostel = hostel; }
    public void setRoomNo(String roomNo) { this.roomNo = roomNo; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public void setSpecialty(String specialty) { this.specialty = specialty; }
    public void setIsAvailable(Boolean isAvailable) { this.isAvailable = isAvailable; }
}