package com.msconnect.maintenancebackend.controller;

import com.msconnect.maintenancebackend.dto.ReportResponse;
import com.msconnect.maintenancebackend.repository.ReportRepository;
import com.msconnect.maintenancebackend.entity.Report;
import com.msconnect.maintenancebackend.entity.User;
import com.msconnect.maintenancebackend.repository.UserRepository;
import com.msconnect.maintenancebackend.service.ReportService;
import com.msconnect.maintenancebackend.service.UserService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
public class ReportController {

    private final ReportService reportService;
    private final UserService userService;
    private final UserRepository userRepository;
    private final ReportRepository reportRepository;
    private static final String UPLOAD_DIR = "uploads/";

    public ReportController(ReportService reportService, UserService userService, UserRepository userRepository, ReportRepository reportRepository) {
        this.reportService = reportService;
        this.userService = userService;
        this.userRepository = userRepository;
        this.reportRepository = reportRepository;
    }
@PostMapping(value = "/submit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
public ResponseEntity<?> submitReportMultipart(
        @RequestParam("category") String category,
        @RequestParam("blockLandmark") String blockLandmark,
        @RequestParam("roomNumber") String roomNumber,
        @RequestParam("description") String description,
        @RequestParam(value = "userId", required = false) Long userId,
        @RequestParam(value = "mediaFile", required = false) MultipartFile mediaFile,
        @RequestParam(value = "visibility", required = false, defaultValue = "public") String visibility,
        @RequestParam(value = "latitude", required = false) Double latitude,
        @RequestParam(value = "longitude", required = false) Double longitude
) {
    try {
        Report report = new Report();

        // Fetch student account
        User user = null;
        if (userId != null) {
            user = userService.getUserById(userId).orElse(null);
            if (user == null) {
                user = userRepository.findById(userId).orElse(null);
            }
        }

        // Fallback student
        if (user == null) {
            user = userRepository.findAll().stream()
                    .filter(u -> "student".equalsIgnoreCase(u.getRole()))
                    .reduce((first, second) -> second)
                    .orElseGet(() -> {
                        User defaultStudent = new User();
                        defaultStudent.setName("Shantel Ama");
                        defaultStudent.setEmail("shantel@gmail.com");
                        defaultStudent.setRole("student");
                        defaultStudent.setPasswordHash("password");
                        return userRepository.save(defaultStudent);
                    });
        }

        report.setUser(user);
        report.setCategory(category);
        report.setBlockLandmark(blockLandmark);
        report.setRoomNumber(roomNumber);
        report.setDescription(description);
        report.setVisibility(visibility);
        
        if (latitude != null) report.setLatitude(latitude);
        if (longitude != null) report.setLongitude(longitude);

        // Save media file
        if (mediaFile != null && !mediaFile.isEmpty()) {
            String originalFilename = mediaFile.getOriginalFilename();
            String cleanFileName = System.currentTimeMillis() + "_" + (originalFilename != null ? originalFilename.replaceAll("[^a-zA-Z0-9\\.\\-_]", "_") : "attachment");
            File uploadFolder = new File(UPLOAD_DIR);
            if (!uploadFolder.exists()) {
                uploadFolder.mkdirs();
            }
            Path filePath = Paths.get(UPLOAD_DIR + cleanFileName);
            Files.write(filePath, mediaFile.getBytes());
            report.setImageUrl("/uploads/" + cleanFileName);
        }

        // ✅ Set priority
        report.setPriority(determinePriority(category));

        // ✅ DO NOT set status here - let the service handle it
        // Service will set to "in_progress" automatically

        Report savedReport = reportService.createReport(report);
        
        return ResponseEntity.ok(ReportResponse.fromEntity(savedReport));
        
    } catch (IOException e) {
        e.printStackTrace();
        return ResponseEntity.status(500).body(Map.of("error", "Failed to store media file: " + e.getMessage()));
    } catch (Exception e) {
        e.printStackTrace();
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
}


    // Find technician based on category matching specialty
   private User findTechnicianByCategory(String category) {
    List<User> technicians = userService.getTechnicians();
    
    if (technicians.isEmpty()) {
        System.out.println("⚠️ No technicians found in database!");
        return null;
    }

    System.out.println("🔍 Looking for technician for category: " + category);
    System.out.println("🔍 Total technicians: " + technicians.size());

    String categoryLower = category.toLowerCase().trim();

    // ✅ Try to find a technician with matching specialty
    for (User tech : technicians) {
        String specialty = tech.getSpecialty() != null ? tech.getSpecialty().toLowerCase().trim() : "";
        if (!specialty.isEmpty() && (specialty.contains(categoryLower) || categoryLower.contains(specialty))) {
            System.out.println("✅ Found matching technician: " + tech.getName() + 
                             " (Specialty: " + tech.getSpecialty() + ")");
            return tech;
        }
    }

    // ✅ If no specialty match, return the first available technician
    User defaultTech = technicians.get(0);
    System.out.println("⚠️ No specialty match for '" + category + "', assigning to: " + defaultTech.getName());
    return defaultTech;
}
    private String determinePriority(String category) {
    if ("Electrical".equalsIgnoreCase(category) || "Plumbing".equalsIgnoreCase(category)) {
        return "high";
    } else if ("IT / Wi-Fi".equalsIgnoreCase(category) || "Masonry".equalsIgnoreCase(category)) {
        return "medium";
    }
    return "medium";
}
     
    // Add this endpoint to ReportController.java
    @GetMapping("/sla-analytics")
    public ResponseEntity<Map<String, Object>> getSlaAnalytics() {
        return ResponseEntity.ok(reportService.getSlaAnalytics());
    }

    @GetMapping("/technician/{technicianId}")
    public ResponseEntity<List<ReportResponse>> getReportsByTechnician(@PathVariable Long technicianId) {
        List<Report> reports = reportRepository.findByAssignedToId(technicianId);
        return ResponseEntity.ok(reports.stream()
                .map(ReportResponse::fromEntity)
                .collect(Collectors.toList()));
    }

    @GetMapping("/technician/{technicianId}/sla-violations")
    public ResponseEntity<?> getSlaViolations(@PathVariable Long technicianId) {
        try {
            List<Report> reports = reportRepository.findByAssignedToId(technicianId);
            
            List<Map<String, Object>> violations = new ArrayList<>();
            LocalDateTime now = LocalDateTime.now();
            
            for (Report report : reports) {
                if ("resolved".equalsIgnoreCase(report.getStatus()) || 
                    "completed".equalsIgnoreCase(report.getStatus())) {
                    continue;
                }
                
                LocalDateTime deadline = report.getSlaDeadline();
                if (deadline == null) {
                    deadline = report.getCreatedAt().plusDays(14);
                }
                
                if (now.isAfter(deadline)) {
                    Map<String, Object> violation = new HashMap<>();
                    violation.put("id", report.getId());
                    violation.put("category", report.getCategory());
                    violation.put("description", report.getDescription());
                    violation.put("blockLandmark", report.getBlockLandmark());
                    violation.put("roomNumber", report.getRoomNumber());
                    violation.put("status", report.getStatus());
                    violation.put("priority", report.getPriority());
                    violation.put("createdAt", report.getCreatedAt());
                    violation.put("deadline", deadline);
                    violation.put("daysOverdue", ChronoUnit.DAYS.between(deadline, now));
                    violation.put("imageUrl", report.getImageUrl());
                    
                    report.setSlaStatus("violated");
                    reportRepository.save(report);
                    
                    violations.add(violation);
                }
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("violations", violations);
            response.put("totalViolations", violations.size());
            response.put("message", violations.isEmpty() ? "No SLA violations found" : "SLA violations found");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/accept")
    public ResponseEntity<?> acceptReport(@PathVariable Long id) {
        try {
            Report report = reportService.getReportById(id)
                    .orElseThrow(() -> new RuntimeException("Report not found"));
            report.setStatus("in_progress");
            Report updated = reportService.createReport(report);
            return ResponseEntity.ok(ReportResponse.fromEntity(updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<?> rejectReport(@PathVariable Long id) {
        try {
            Report report = reportService.getReportById(id)
                    .orElseThrow(() -> new RuntimeException("Report not found"));

            Long currentTechId = report.getAssignedTo() != null ? report.getAssignedTo().getId() : null;

            List<User> technicians = userService.getTechnicians();
            User nextTech = technicians.stream()
                    .filter(t -> !t.getId().equals(currentTechId))
                    .findFirst()
                    .orElse(null);

            if (nextTech != null) {
                report.setAssignedTo(nextTech);
                report.setStatus("PENDING_ACCEPTANCE");
            } else {
                report.setAssignedTo(null);
                report.setStatus("pending");
            }

            Report updated = reportService.createReport(report);
            return ResponseEntity.ok(ReportResponse.fromEntity(updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateReportStatus(@PathVariable Long id, @RequestBody Map<String, String> request) {
        try {
            String status = request.get("status");
            Report updated = reportService.updateReportStatus(id, status);
            return ResponseEntity.ok(ReportResponse.fromEntity(updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ReportResponse>> getReportsByUser(@PathVariable Long userId) {
        List<Report> reports = reportRepository.findByUserId(userId);
        return ResponseEntity.ok(reports.stream()
                .map(ReportResponse::fromEntity)
                .collect(Collectors.toList()));
    }

    @GetMapping
    public ResponseEntity<List<ReportResponse>> getAllReports() {
        List<Report> reports = reportService.getAllReports();
        return ResponseEntity.ok(reports.stream()
                .map(ReportResponse::fromEntity)
                .collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getReportById(@PathVariable Long id) {
        return reportService.getReportById(id)
                .map(report -> ResponseEntity.ok(ReportResponse.fromEntity(report)))
                .orElse(ResponseEntity.notFound().build());
    }

 @GetMapping("/stats")
public ResponseEntity<Map<String, Object>> getStats() {
    Map<String, Object> stats = new HashMap<>();
    List<Report> allReports = reportService.getAllReports();
    
    long total = allReports.size();
    long resolved = allReports.stream().filter(r -> "resolved".equalsIgnoreCase(r.getStatus())).count();
    long inProgress = allReports.stream().filter(r -> "in_progress".equalsIgnoreCase(r.getStatus())).count();
    long pending = allReports.stream().filter(r -> "pending".equalsIgnoreCase(r.getStatus()) || 
                                                    "pending_acceptance".equalsIgnoreCase(r.getStatus())).count();
    long technicians = userService.getTechnicians().size();
    
    stats.put("totalReports", total);
    stats.put("resolvedReports", resolved);
    stats.put("inProgressReports", inProgress);
    stats.put("pendingReports", pending);
    stats.put("totalTechnicians", technicians);
    
    return ResponseEntity.ok(stats);
    
}
}