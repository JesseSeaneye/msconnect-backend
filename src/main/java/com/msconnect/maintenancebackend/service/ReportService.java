package com.msconnect.maintenancebackend.service;

import com.msconnect.maintenancebackend.entity.Report;
import com.msconnect.maintenancebackend.entity.User;
import com.msconnect.maintenancebackend.repository.ReportRepository;
import com.msconnect.maintenancebackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ReportService {

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private UserRepository userRepository;

    public Report createReport(Report report) {
    Report savedReport = reportRepository.save(report);
    // Auto-assign the nearest technician
    User technician = findNearestTechnician(report.getLatitude(), report.getLongitude());
    savedReport.setAssignedTo(technician);
    savedReport.setStatus("in_progress");
    return reportRepository.save(savedReport);
}
private User findNearestTechnician(Double latitude, Double longitude) {
    if (latitude == null || longitude == null) {
        // Return the first available technician if no location provided
        return userRepository.findByRole("technician")
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No technicians available"));
    }
    
    List<User> technicians = userRepository.findByRole("technician");
    
    if (technicians.isEmpty()) {
        throw new RuntimeException("No technicians available");
    }
    
    // Find the closest technician using Haversine formula
    User nearest = null;
    double minDistance = Double.MAX_VALUE;
    
    for (User tech : technicians) {
        if (tech.getLatitude() != null && tech.getLongitude() != null) {
            double distance = calculateDistance(
                latitude, longitude,
                tech.getLatitude(), tech.getLongitude()
            );
            if (distance < minDistance) {
                minDistance = distance;
                nearest = tech;
            }
        }
    }
    
    // If no technician has location data, return the first one
    if (nearest == null && !technicians.isEmpty()) {
        nearest = technicians.get(0);
    }
    
    return nearest;
}

private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
    double R = 6371; // Earth's radius in km
    double dLat = Math.toRadians(lat2 - lat1);
    double dLon = Math.toRadians(lon2 - lon1);
    double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
               Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
               Math.sin(dLon/2) * Math.sin(dLon/2);
    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
    return R * c;
}
    public List<Report> getAllReports() {
        return reportRepository.findAll();
    }

    public Optional<Report> getReportById(Long id) {
        return reportRepository.findById(id);
    }

    public List<Report> getReportsByUser(Long userId) {
        return reportRepository.findByUserId(userId);
    }

    public List<Report> getReportsByStatus(String status) {
        return reportRepository.findByStatus(status);
    }

    public List<Report> getReportsByTechnician(Long technicianId) {
        return reportRepository.findByAssignedToId(technicianId);
    }

    public Report updateReportStatus(Long reportId, String status) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found"));
        report.setStatus(status);
        return reportRepository.save(report);
    }

    public Report assignTechnician(Long reportId, Long technicianId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found"));

        User technician = userRepository.findById(technicianId)
                .orElseThrow(() -> new RuntimeException("Technician not found"));

        if (!"technician".equals(technician.getRole())) {
            throw new RuntimeException("User is not a technician");
        }

        report.setAssignedTo(technician);
        report.setStatus("in_progress");
        return reportRepository.save(report);
    }
public void deleteReport(Long id) {
    if (!reportRepository.existsById(id)) {
        throw new RuntimeException("Report not found with id: " + id);
    }
    reportRepository.deleteById(id);
}
public List<Report> getReportsByPriority(String priority) {
    return reportRepository.findByPriority(priority);
}
}
