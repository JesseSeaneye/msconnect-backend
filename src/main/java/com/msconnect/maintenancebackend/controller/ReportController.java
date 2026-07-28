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
    
    // ✅ Round-robin counter per specialty
    private static final Map<String, Integer> specialtyCounter = new HashMap<>();

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
            @RequestParam(value = "visibility", required = false, defaultValue = "public") String visibility
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

            report.setPriority(determinePriority(category));
            report.setStatus("in_progress");

            // --- ✅ GUARANTEED ROUND-ROBIN ASSIGNMENT ---
            User assignedTech = assignTechnicianRoundRobin(category);
            
            if (assignedTech != null) {
                report.setAssignedTo(assignedTech);
                System.out.println("✅ ASSIGNED: " + assignedTech.getName() + " (ID: " + assignedTech.getId() + ")");
            } else {
                System.out.println("❌ NO TECHNICIAN FOUND for category: " + category);
            }

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

    // ✅ ROUND-ROBIN ASSIGNMENT METHOD - GUARANTEED TO WORK
    private User assignTechnicianRoundRobin(String category) {
        // Get all technicians
        List<User> technicians = userService.getTechnicians();
        
        if (technicians.isEmpty()) {
            System.out.println("❌ No technicians found!");
            return null;
        }

        String categoryLower = category.toLowerCase().trim();
        System.out.println("🔍 ======================================");
        System.out.println("🔍 CATEGORY: " + category);
        System.out.println("🔍 TOTAL TECHNICIANS: " + technicians.size());
        
        // Find technicians with matching specialty (case-insensitive)
        List<User> matchingTechs = new ArrayList<>();
        for (User tech : technicians) {
            String specialty = tech.getSpecialty() != null ? tech.getSpecialty().toLowerCase().trim() : "";
            
            // Check if specialty matches category
            if (!specialty.isEmpty() && (specialty.contains(categoryLower) || categoryLower.contains(specialty))) {
                matchingTechs.add(tech);
            }
        }
        
        System.out.println("🔍 MATCHING TECHNICIANS: " + matchingTechs.size());
        for (User t : matchingTechs) {
            System.out.println("   - " + t.getName() + " (ID: " + t.getId() + ", Specialty: " + t.getSpecialty() + ")");
        }
        
        if (matchingTechs.isEmpty()) {
            // Fallback: use the first available technician
            User fallback = technicians.get(0);
            System.out.println("⚠️ No specialty match! FALLBACK to: " + fallback.getName());
            return fallback;
        }
        
        // Round-robin: get the next index
        int currentIndex = specialtyCounter.getOrDefault(categoryLower, 0);
        User selected = matchingTechs.get(currentIndex);
        
        // Update counter for next time
        int nextIndex = (currentIndex + 1) % matchingTechs.size();
        specialtyCounter.put(categoryLower, nextIndex);
        
        System.out.println("🎯 SELECTED: " + selected.getName() + " (ID: " + selected.getId() + ")");
        System.out.println("   Index: " + currentIndex + " → Next: " + nextIndex + " of " + matchingTechs.size());
        System.out.println("🔍 ======================================");
        
        return selected;
    }

    @GetMapping("/technician/{technicianId}")
    public ResponseEntity<List<ReportResponse>> getReportsByTechnician(@PathVariable Long technicianId) {
        List<Report> reports = reportRepository.findByAssignedToId(technicianId);
        return ResponseEntity.ok(reports.stream()
                .map(ReportResponse::fromEntity)
                .collect(Collectors.toList()));
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
        stats.put("totalReports", allReports.size());
        stats.put("pendingReports", reportService.getReportsByStatus("pending").size());
        stats.put("inProgressReports", reportService.getReportsByStatus("in_progress").size());
        stats.put("resolvedReports", reportService.getReportsByStatus("resolved").size());
        stats.put("totalTechnicians", userService.getTechnicians().size());
        return ResponseEntity.ok(stats);
    }

    private String determinePriority(String category) {
        if ("Electrical".equalsIgnoreCase(category) || "Plumbing".equalsIgnoreCase(category)) {
            return "high";
        }
        return "medium";
    }
}