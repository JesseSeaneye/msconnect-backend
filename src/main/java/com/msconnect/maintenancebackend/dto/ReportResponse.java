package com.msconnect.maintenancebackend.dto;

import com.msconnect.maintenancebackend.entity.Report;
import java.time.LocalDateTime;

public class ReportResponse {
    private Long id;
    
    // Student Information
    private Long userId;
    private String userName;
    private String userEmail;
    
    // Ticket Details
    private String category;
    private String blockLandmark;
    private String roomNumber;
    private String imageUrl;
    private String description;
    private Double latitude;
    private Double longitude;
    private String status;
    private String priority;
    
    // Assigned Technician Information
    private Long assignedToId;
    private String assignedToName;
    private String assignedToEmail;
    private String assignedToPhone;
    
    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
     
    
    private String visibility;
    
    // Default Constructor
    public ReportResponse() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getBlockLandmark() { return blockLandmark; }
    public void setBlockLandmark(String blockLandmark) { this.blockLandmark = blockLandmark; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public Long getAssignedToId() { return assignedToId; }
    public void setAssignedToId(Long assignedToId) { this.assignedToId = assignedToId; }

    public String getAssignedToName() { return assignedToName; }
    public void setAssignedToName(String assignedToName) { this.assignedToName = assignedToName; }

    public String getAssignedToEmail() { return assignedToEmail; }
    public void setAssignedToEmail(String assignedToEmail) { this.assignedToEmail = assignedToEmail; }

    public String getAssignedToPhone() { return assignedToPhone; }
    public void setAssignedToPhone(String assignedToPhone) { this.assignedToPhone = assignedToPhone; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getVisibility() { return visibility; }
    public void setVisibility(String visibility) { this.visibility = visibility; }
    // Static Converter Method
    public static ReportResponse fromEntity(Report report) {
        ReportResponse response = new ReportResponse();
        response.setId(report.getId());
        response.setCategory(report.getCategory());
        response.setBlockLandmark(report.getBlockLandmark());
        response.setRoomNumber(report.getRoomNumber());
        response.setImageUrl(report.getImageUrl());
        response.setDescription(report.getDescription());
        response.setLatitude(report.getLatitude());
        response.setLongitude(report.getLongitude());
        response.setVisibility(report.getVisibility());
        // Preserve exact status ("PENDING_ACCEPTANCE", "in_progress", etc.)
        response.setStatus(report.getStatus() != null ? report.getStatus() : "PENDING_ACCEPTANCE");
        response.setPriority(report.getPriority());
        response.setCreatedAt(report.getCreatedAt());
        response.setUpdatedAt(report.getUpdatedAt());

        // Bind Student User DTO Fields
        if (report.getUser() != null) {
            response.setUserId(report.getUser().getId());
            response.setUserName(report.getUser().getName());
            response.setUserEmail(report.getUser().getEmail());
        }

        // Bind Assigned Technician DTO Fields
        if (report.getAssignedTo() != null) {
            response.setAssignedToId(report.getAssignedTo().getId());
            response.setAssignedToName(report.getAssignedTo().getName());
            response.setAssignedToEmail(report.getAssignedTo().getEmail());
            response.setAssignedToPhone(report.getAssignedTo().getEmail()); // Fallback to email
        }

        return response;
    }
}