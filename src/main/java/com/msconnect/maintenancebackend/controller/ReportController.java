package com.msconnect.maintenancebackend.controller;

import com.msconnect.maintenancebackend.dto.ReportResponse;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
public class ReportController {

    private final ReportService reportService;
    private final UserService userService;
    private final UserRepository userRepository;

    private static final String UPLOAD_DIR = "uploads/";

    public ReportController(ReportService reportService, UserService userService, UserRepository userRepository) {
        this.reportService = reportService;
        this.userService = userService;
        this.userRepository = userRepository;
    }

    // --- 1. SUBMIT REPORT (PURE DYNAMIC SPECIALTY AUTO-DISPATCH & MEDIA STORAGE) ---
    @PostMapping(value = "/submit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> submitReportMultipart(
            @RequestParam("category") String category,
            @RequestParam("blockLandmark") String blockLandmark,
            @RequestParam("roomNumber") String roomNumber,
            @RequestParam("description") String description,
            @RequestParam(value = "userId", required = false) Long userId,
            @RequestParam(value = "mediaFile", required = false) MultipartFile mediaFile
    ) {
        try {
            Report report = new Report();

            // Fetch student account passed by ID
            User user = null;
            if (userId != null) {
                user = userService.getUserById(userId).orElse(null);
                if (user == null) {
                    user = userRepository.findById(userId).orElse(null);
                }
            }

            // Fallback student link to prevent PostgreSQL user_id NOT NULL errors
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

            // Save Photo or Video Attachment (e.g. .jpg, .png, .mp4, .mov)
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
            
            // Strictly set status to PENDING_ACCEPTANCE for new submissions
            report.setStatus("PENDING_ACCEPTANCE");

            // --- 🎯 PURE DYNAMIC SPECIALTY AUTO-DISPATCH LOGIC ---
            List<User> technicians = userService.getTechnicians();

            User matchingTech = technicians.stream()
                    .filter(t -> {
                        String cat = category.toLowerCase().trim();
                        String specialty = t.getSpecialty() != null ? t.getSpecialty().toLowerCase().trim() : "";
                        String name = t.getName() != null ? t.getName().toLowerCase().trim() : "";
                        String email = t.getEmail() != null ? t.getEmail().toLowerCase().trim() : "";

                        // 1. Direct Specialty Match (e.g., category 'Masonry' matches specialty 'Masonry')
                        if (!specialty.isEmpty() && (specialty.contains(cat) || cat.contains(specialty))) {
                            return true;
                        }

                        // 2. Name or Email Match
                        return name.contains(cat) || email.contains(cat);
                    })
                    .findFirst()
                    .orElse(null);

            // Fallback: Assign to first available registered technician if no exact specialty match exists
            if (matchingTech == null && !technicians.isEmpty()) {
                matchingTech = technicians.get(0);
            }

            if (matchingTech != null) {
                report.setAssignedTo(matchingTech);
            }

            Report savedReport = reportService.createReport(report);
            
            // Enforce status explicitly before returning DTO
            savedReport.setStatus("PENDING_ACCEPTANCE");
            
            return ResponseEntity.ok(ReportResponse.fromEntity(savedReport));
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Failed to store media file: " + e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // --- 2. TECHNICIAN DISPATCH BOARD FETCH ---
    @GetMapping("/technician/{technicianId}")
    public ResponseEntity<List<ReportResponse>> getReportsByTechnician(@PathVariable Long technicianId) {
        // Fetch ONLY reports strictly assigned to this technician ID
        List<Report> reports = reportService.getAllReports().stream()
                .filter(r -> r.getAssignedTo() != null && r.getAssignedTo().getId().equals(technicianId))
                .collect(Collectors.toList());

        return ResponseEntity.ok(reports.stream()
                .map(ReportResponse::fromEntity)
                .collect(Collectors.toList()));
    }

    // --- 3. ACCEPT TASK ENDPOINT ---
    @PutMapping("/{id}/accept")
    public ResponseEntity<?> acceptReport(@PathVariable Long id) {
        try {
            Report report = reportService.getReportById(id)
                    .orElseThrow(() -> new RuntimeException("Report not found"));

            report.setStatus("in_progress"); // Confirmed by technician!
            Report updated = reportService.createReport(report);
            return ResponseEntity.ok(ReportResponse.fromEntity(updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // --- 4. REJECT TASK ENDPOINT ---
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

    // --- STUDENT & ADMIN ENDPOINTS ---

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ReportResponse>> getReportsByUser(@PathVariable Long userId) {
        List<Report> reports = reportService.getReportsByUser(userId);
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