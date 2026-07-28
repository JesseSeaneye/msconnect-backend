package com.msconnect.maintenancebackend.repository;

import com.msconnect.maintenancebackend.entity.Report;
import com.msconnect.maintenancebackend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

    // --- STUDENT LOOKUPS ---
    List<Report> findByUser(User user);  // ✅ This matches controller
    List<Report> findByUserId(Long userId);
    List<Report> findByUserEmail(String email);

    // --- TECHNICIAN LOOKUPS ---
    List<Report> findByAssignedTo(User technician);  // ✅ This matches controller
    List<Report> findByAssignedToId(Long technicianId);
    List<Report> findByAssignedToIdAndStatus(Long technicianId, String status);
    List<Report> findByAssignedToIdAndStatusIgnoreCase(Long technicianId, String status);
    
    // --- CATEGORY & PRIORITY FILTERS ---
    List<Report> findByStatus(String status);
    List<Report> findByStatusIgnoreCase(String status);
    List<Report> findByPriority(String priority);

    // --- ADMIN OVERVIEW ---
    List<Report> findAllByOrderByCreatedAtDesc();

    List<Report> findByVisibility(String visibility);
}  