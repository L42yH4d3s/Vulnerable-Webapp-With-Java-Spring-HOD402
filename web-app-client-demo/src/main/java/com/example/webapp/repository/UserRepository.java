package com.example.webapp.repository;

import com.example.webapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.webapp.model.Role;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    // Safe JPA repository methods
    User findByUsername(String username);
    List<User> findByRole(Role role);
} 