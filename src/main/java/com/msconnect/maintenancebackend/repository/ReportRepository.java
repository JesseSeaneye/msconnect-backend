package com.msconnect.maintenancebackend.repository;

import com.msconnect.maintenancebackend.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

    // --- STUDENT LOOKUPS ---
    // Fetch reports submitted by a specific student using their User ID
    List<Report> findByUserId(Long userId);

    // Fetch reports submitted by a student using their Email address
    List<Report> findByUserEmail(String email);


    // --- TECHNICIAN LOOKUPS ---
    // Fetch all tasks assigned to a specific technician
    List<Report> findByAssignedToId(Long technicianId);

    // Fetch tasks assigned to a technician filtered by ticket status
    List<Report> findByAssignedToIdAndStatus(Long technicianId, String status);

    // Case-insensitive lookup for technician tasks by status
    List<Report> findByAssignedToIdAndStatusIgnoreCase(Long technicianId, String status);


    // --- CATEGORY & PRIORITY FILTERS ---
    List<Report> findByStatus(String status);

    List<Report> findByStatusIgnoreCase(String status);

    List<Report> findByPriority(String priority);


    // --- ADMIN OVERVIEW ---
    // Fetch all reports across the entire system ordered newest to oldest
    List<Report> findAllByOrderByCreatedAtDesc();
}