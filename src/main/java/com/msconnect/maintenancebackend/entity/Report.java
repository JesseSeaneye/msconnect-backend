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
    @JoinColumn(name = "user_id", nullable = true) // Set to true to prevent non-null constraint exceptions
    private User user; // The Student reporting the issue

    @Column(nullable = false)
    private String category; // e.g., "Electrical", "Plumbing", "Carpentry", "Masonry"

    @Column(name = "block_landmark", nullable = false)
    private String blockLandmark; // e.g., "Unity Hall, Block C"

    @Column(name = "room_number", nullable = false)
    private String roomNumber; // e.g., "42"

    @Column(name = "image_url")
    private String imageUrl; // Store local files path or web URLs

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    private Double latitude;
    private Double longitude;

    @Column(nullable = false)
    private String status = "PENDING_ACCEPTANCE"; // ✅ DEFAULT SET TO PENDING_ACCEPTANCE

    @Column(nullable = false)
    private String priority = "medium";

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "assigned_to")
    private User assignedTo; // The auto-dispatched technician

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null || status.trim().isEmpty()) {
            status = "PENDING_ACCEPTANCE"; // ✅ ENFORCES PENDING_ACCEPTANCE ON JPA PERSIST
        }
        if (priority == null || priority.trim().isEmpty()) {
            priority = "medium";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Default Constructor
    public Report() {}

    // Getters
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

    // Setters
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
}