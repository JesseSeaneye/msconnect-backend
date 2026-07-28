package com.msconnect.maintenancebackend.repository;

import com.msconnect.maintenancebackend.entity.AssignmentTracker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface AssignmentTrackerRepository extends JpaRepository<AssignmentTracker, Long> {
    Optional<AssignmentTracker> findBySpecialty(String specialty);
}