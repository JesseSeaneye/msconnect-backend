package com.msconnect.maintenancebackend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "report_media")
public class ReportMedia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Change "MaintenanceReport" to "Report" here 👇
    @ManyToOne
    @JoinColumn(name = "report_id", nullable = false)
    private Report report;

    @Column(name = "media_url", nullable = false)
    private String mediaUrl;

    @Column(name = "media_type")
    private String mediaType; // "image" or "video"

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    // === Update your Getters and Setters to use Report instead of MaintenanceReport ===

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    // Change return type to Report 👇
    public Report getReport() {
        return report;
    }

    // Change parameter type to Report 👇
    public void setReport(Report report) {
        this.report = report;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}