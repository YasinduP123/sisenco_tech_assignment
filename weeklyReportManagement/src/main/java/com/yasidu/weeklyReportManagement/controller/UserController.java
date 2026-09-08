package com.yasidu.weeklyReportManagement.controller;

import com.yasidu.weeklyReportManagement.dto.UserDto;
import com.yasidu.weeklyReportManagement.enums.UserRole;
import com.yasidu.weeklyReportManagement.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // ---- Any authenticated user ----

    @GetMapping("/me")
    public ResponseEntity<UserDto> getMyProfile() {
        return ResponseEntity.ok(userService.getCurrentUserProfile());
    }

    // ---- Manager-only ----

    @GetMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/team-members")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<UserDto>> getAllTeamMembers() {
        return ResponseEntity.ok(userService.getAllTeamMembers());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PatchMapping("/{id}/role")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<UserDto> updateUserRole(
            @PathVariable Long id,
            @RequestBody Map<String, UserRole> body) {
        UserRole newRole = body.get("role");
        return ResponseEntity.ok(userService.updateUserRole(id, newRole));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}