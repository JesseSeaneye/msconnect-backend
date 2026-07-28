package com.msconnect.maintenancebackend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "assignment_tracker")
public class AssignmentTracker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String specialty;

    @Column(name = "last_technician_id")
    private Long lastTechnicianId;

    public AssignmentTracker() {}

    public AssignmentTracker(String specialty, Long lastTechnicianId) {
        this.specialty = specialty;
        this.lastTechnicianId = lastTechnicianId;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSpecialty() { return specialty; }
    public void setSpecialty(String specialty) { this.specialty = specialty; }

    public Long getLastTechnicianId() { return lastTechnicianId; }
    public void setLastTechnicianId(Long lastTechnicianId) { this.lastTechnicianId = lastTechnicianId; }
}