package com.msconnect.maintenancebackend.dto;

public class ReportRequest {
    private Long userId;
    private String imageUrl;
    private String description;
    private Double latitude;
    private Double longitude;
    private String priority;

    // Getters
    public Long getUserId() { return userId; }
    public String getImageUrl() { return imageUrl; }
    public String getDescription() { return description; }
    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }
    public String getPriority() { return priority; }

    // Setters
    public void setUserId(Long userId) { this.userId = userId; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setDescription(String description) { this.description = description; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public void setPriority(String priority) { this.priority = priority; }
}