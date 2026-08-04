package com.msconnect.maintenancebackend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reports")
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = true)
    private User user;

    @Column(nullable = false)
    private String category;

    @Column(name = "block_landmark", nullable = false)
    private String blockLandmark;

    @Column(name = "room_number", nullable = false)
    private String roomNumber;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;
     
    @Column(name = "visibility", nullable = false)
    private String visibility = "public";

    private Double latitude;
    private Double longitude;

    @Column(nullable = false)
    private String status = "PENDING_ACCEPTANCE";

    @Column(nullable = false)
    private String priority = "medium";

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "assigned_to")
    private User assignedTo;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null || status.trim().isEmpty()) {
            status = "PENDING_ACCEPTANCE";
        }
        if (priority == null || priority.trim().isEmpty()) {
            priority = "medium";
        }
        if (visibility == null || visibility.trim().isEmpty()) {
            visibility = "public";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @Column(name = "sla_deadline")
    private LocalDateTime slaDeadline;

    @Column(name = "sla_status")
    private String slaStatus = "on_track";

    @Column(name = "response_time")
    private Integer responseTime;

    @Column(name = "resolution_time")
    private Integer resolutionTime;
        // Default Constructor
        public Report() {}

    // --- GETTERS ---
    public Long getId() { return id; }
    public User getUser() { return user; }
    public String getCategory() { return category; }
    public String getBlockLandmark() { return blockLandmark; }
    public String getRoomNumber() { return roomNumber; }
    public String getImageUrl() { return imageUrl; }
    public String getDescription() { return description; }
    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }
    public String getStatus() { return status; }
    public String getPriority() { return priority; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public User getAssignedTo() { return assignedTo; }
    public String getVisibility() { return visibility; }  // ✅ KEEP ONLY ONE
    public LocalDateTime getSlaDeadline() { return slaDeadline; }
    public String getSlaStatus() { return slaStatus; }
    public Integer getResponseTime() { return responseTime; }
    public Integer getResolutionTime() { return resolutionTime; }



    // --- SETTERS ---
    public void setId(Long id) { this.id = id; }
    public void setUser(User user) { this.user = user; }
    public void setCategory(String category) { this.category = category; }
    public void setBlockLandmark(String blockLandmark) { this.blockLandmark = blockLandmark; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setDescription(String description) { this.description = description; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public void setStatus(String status) { this.status = status; }
    public void setPriority(String priority) { this.priority = priority; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public void setAssignedTo(User assignedTo) { this.assignedTo = assignedTo; }
    public void setVisibility(String visibility) { this.visibility = visibility; }
    public void setSlaDeadline(LocalDateTime slaDeadline) { this.slaDeadline = slaDeadline; }
    public void setSlaStatus(String slaStatus) { this.slaStatus = slaStatus; }
    public void setResponseTime(Integer responseTime) { this.responseTime = responseTime; }
    public void setResolutionTime(Integer resolutionTime) { this.resolutionTime = resolutionTime; }
}