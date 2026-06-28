package com.msconnect.maintenancebackend.dto;

import com.msconnect.maintenancebackend.entity.Report;
import java.time.LocalDateTime;

public class ReportResponse {
    private Long id;
    private String userName;
    private String userEmail;
    private String imageUrl;
    private String description;
    private Double latitude;
    private Double longitude;
    private String status;
    private String priority;
    private String assignedToName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

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

    public String getAssignedToName() { return assignedToName; }
    public void setAssignedToName(String assignedToName) { this.assignedToName = assignedToName; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // Static method to convert from Report entity
    public static ReportResponse fromEntity(Report report) {
        ReportResponse response = new ReportResponse();
        response.setId(report.getId());
        response.setImageUrl(report.getImageUrl());
        response.setDescription(report.getDescription());
        response.setLatitude(report.getLatitude());
        response.setLongitude(report.getLongitude());
        response.setStatus(report.getStatus());
        response.setPriority(report.getPriority());
        response.setCreatedAt(report.getCreatedAt());
        response.setUpdatedAt(report.getUpdatedAt());

        if (report.getUser() != null) {
            response.setUserName(report.getUser().getName());
            response.setUserEmail(report.getUser().getEmail());
        }

        if (report.getAssignedTo() != null) {
            response.setAssignedToName(report.getAssignedTo().getName());
        }

        return response;
    }
}