package com.yasidu.weeklyReportManagement.repository;

import com.yasidu.weeklyReportManagement.entity.User;
import com.yasidu.weeklyReportManagement.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByKeycloakId(String keycloakId);
    Optional<User> findByEmail(String email);
    List<User> findByRole(UserRole role);
}