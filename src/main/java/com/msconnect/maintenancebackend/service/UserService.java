package com.msconnect.maintenancebackend.service;

import com.msconnect.maintenancebackend.entity.User;
import com.msconnect.maintenancebackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

   public List<User> getTechnicians() {
    List<User> techs = userRepository.findByRole("technician");
    System.out.println("🔍 UserService.getTechnicians() found: " + techs.size());
    for (User t : techs) {
        System.out.println("   - " + t.getId() + ": " + t.getName());
    }
    return techs;
    }

    public User createUser(User user) {
        return userRepository.save(user);
    }

    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }
}