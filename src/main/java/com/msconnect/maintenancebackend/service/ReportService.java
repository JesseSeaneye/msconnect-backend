package com.msconnect.maintenancebackend.service;

import com.msconnect.maintenancebackend.entity.Report;
import com.msconnect.maintenancebackend.entity.User;
import com.msconnect.maintenancebackend.repository.ReportRepository;
import com.msconnect.maintenancebackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ReportService {

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private UserRepository userRepository;

    private int technicianRoundRobinCounter = 0;

    public Report createReport(Report report) {
        if (report.getAssignedTo() == null) {
            User technician = getNextTechnicianByCategory(report.getCategory());
            if (technician != null) {
                report.setAssignedTo(technician);
            }
        }
        
        // ✅ Set SLA deadline (14 days from now)
        if (report.getSlaDeadline() == null) {
            report.setSlaDeadline(LocalDateTime.now().plusDays(14));
        }
        report.setSlaStatus("on_track");
        
        // ✅ Set response time (time from creation to assignment)
        if (report.getResponseTime() == null && report.getAssignedTo() != null) {
            report.setResponseTime(0); // Immediate assignment
        }
        
        report.setStatus("in_progress");
        return reportRepository.save(report);
    }

    // ✅ NEW: Get SLA statistics with real data
    public java.util.Map<String, Object> getSlaAnalytics() {
        java.util.Map<String, Object> analytics = new java.util.HashMap<>();
        List<Report> allReports = reportRepository.findAll();
        
        long total = allReports.size();
        long resolved = allReports.stream().filter(r -> "resolved".equalsIgnoreCase(r.getStatus())).count();
        long inProgress = allReports.stream().filter(r -> "in_progress".equalsIgnoreCase(r.getStatus())).count();
        long pending = allReports.stream().filter(r -> "pending".equalsIgnoreCase(r.getStatus())).count();
        
        // ✅ Calculate average response time (in hours)
        double avgResponseTime = allReports.stream()
                .filter(r -> r.getResponseTime() != null)
                .mapToInt(Report::getResponseTime)
                .average()
                .orElse(0.0);
        
        // ✅ Calculate average resolution time (in hours) - only resolved reports
        double avgResolutionTime = allReports.stream()
                .filter(r -> "resolved".equalsIgnoreCase(r.getStatus()) && r.getResolutionTime() != null)
                .mapToInt(Report::getResolutionTime)
                .average()
                .orElse(0.0);
        
        // ✅ Calculate SLA compliance rate
        long onTime = allReports.stream()
                .filter(r -> "resolved".equalsIgnoreCase(r.getStatus()))
                .filter(r -> {
                    LocalDateTime deadline = r.getSlaDeadline();
                    return deadline != null && r.getUpdatedAt().isBefore(deadline);
                })
                .count();
        
        double complianceRate = resolved > 0 ? (onTime * 100.0 / resolved) : 0.0;
        
        // ✅ Count overdue tasks
        LocalDateTime now = LocalDateTime.now();
        long overdue = allReports.stream()
                .filter(r -> !"resolved".equalsIgnoreCase(r.getStatus()) && !"completed".equalsIgnoreCase(r.getStatus()))
                .filter(r -> {
                    LocalDateTime deadline = r.getSlaDeadline();
                    return deadline != null && now.isAfter(deadline);
                })
                .count();
        
        analytics.put("totalReports", total);
        analytics.put("resolvedReports", resolved);
        analytics.put("inProgressReports", inProgress);
        analytics.put("pendingReports", pending);
        analytics.put("avgResponseTime", avgResponseTime);
        analytics.put("avgResolutionTime", avgResolutionTime);
        analytics.put("slaComplianceRate", complianceRate);
        analytics.put("overdueTasks", overdue);
        analytics.put("totalTechnicians", userRepository.findByRole("technician").size());
        
        return analytics;
    }

    private User getNextTechnicianByCategory(String category) {
        List<User> technicians = userRepository.findByRole("technician");
        if (technicians.isEmpty()) return null;

        String categoryLower = category.toLowerCase().trim();
        List<User> matchingTechs = technicians.stream()
                .filter(t -> {
                    String specialty = t.getSpecialty() != null ? t.getSpecialty().toLowerCase().trim() : "";
                    return !specialty.isEmpty() && (specialty.contains(categoryLower) || categoryLower.contains(specialty));
                })
                .collect(Collectors.toList());

        if (matchingTechs.isEmpty()) {
            return technicians.get(0);
        }

        int index = technicianRoundRobinCounter % matchingTechs.size();
        User selected = matchingTechs.get(index);
        technicianRoundRobinCounter++;
        return selected;
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
        
        // ✅ Track resolution time when resolved
        if ("resolved".equalsIgnoreCase(status) && !"resolved".equalsIgnoreCase(report.getStatus())) {
            if (report.getCreatedAt() != null) {
                int resolutionMinutes = (int) ChronoUnit.MINUTES.between(report.getCreatedAt(), LocalDateTime.now());
                report.setResolutionTime(resolutionMinutes);
            }
            report.setSlaStatus("completed");
        }
        
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