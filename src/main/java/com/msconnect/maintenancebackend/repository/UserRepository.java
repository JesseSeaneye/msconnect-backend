package com.msconnect.maintenancebackend.repository;

import com.msconnect.maintenancebackend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // --- CASE-INSENSITIVE EMAIL LOOKUPS (Supports Mixed/Capital/Small Letters) ---
    Optional<User> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);

    // --- LEGACY/EXACT MATCH METHODS ---
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    // --- ROLE LOOKUPS ---
    List<User> findByRole(String role);
}