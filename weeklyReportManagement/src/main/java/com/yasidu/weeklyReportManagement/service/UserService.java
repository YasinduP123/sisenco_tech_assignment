package com.yasidu.weeklyReportManagement.service;

import com.yasidu.weeklyReportManagement.dto.UserDto;
import com.yasidu.weeklyReportManagement.entity.User;
import com.yasidu.weeklyReportManagement.enums.UserRole;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

public interface UserService {

    /**
     * Resolves the currently authenticated user's local User entity from the JWT.
     * Auto-provisions (creates) a local profile on first login if one doesn't exist.
     */
    User getCurrentUser();

    /**
     * Returns the current user as a DTO (used by a "my profile" endpoint).
     */
    UserDto getCurrentUserProfile();

    /**
     * Finds a local user by their Keycloak subject id, provisioning one if absent.
     */
    User findOrProvisionByJwt(Jwt jwt);

    /**
     * Manager-only: list all users (for the User Management page).
     */
    List<UserDto> getAllUsers();

    /**
     * Manager-only: get a single team member's profile by id.
     */
    UserDto getUserById(Long userId);

    /**
     * Manager-only: change a user's role (e.g. promote to MANAGER).
     */
    UserDto updateUserRole(Long userId, UserRole newRole);

    /**
     * Manager-only: remove a team member from the system.
     */
    void deleteUser(Long userId);

    public List<UserDto> getAllTeamMembers();
}