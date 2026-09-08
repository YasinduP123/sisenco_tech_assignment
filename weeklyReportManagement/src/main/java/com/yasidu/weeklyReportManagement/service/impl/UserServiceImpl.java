package com.yasidu.weeklyReportManagement.service.impl;

import com.yasidu.weeklyReportManagement.dto.UserDto;
import com.yasidu.weeklyReportManagement.entity.User;
import com.yasidu.weeklyReportManagement.enums.UserRole;
import com.yasidu.weeklyReportManagement.repository.ReportRepository;
import com.yasidu.weeklyReportManagement.repository.UserRepository;
import com.yasidu.weeklyReportManagement.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ReportRepository reportRepository;

    @Override
    public User getCurrentUser() {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return findOrProvisionByJwt(jwt);
    }

    @Override
    public UserDto getCurrentUserProfile() {
        return toDto(getCurrentUser());
    }

    @Override
    @Transactional
    public User findOrProvisionByJwt(Jwt jwt) {
        String keycloakId = jwt.getSubject();

        return userRepository.findByKeycloakId(keycloakId)
                .orElseGet(() -> provisionUser(jwt, keycloakId));
    }

    @Override
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserDto> getAllTeamMembers() {
        return userRepository.findByRole(UserRole.TEAM_MEMBER).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));
        return toDto(user);
    }

    @Override
    @Transactional
    public UserDto updateUserRole(Long userId, UserRole newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));
        user.setRole(newRole);
        return toDto(userRepository.save(user));
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));

        long reportCount = reportRepository.findByUserId(
                userId, org.springframework.data.domain.Pageable.unpaged()
        ).getTotalElements();

        if (reportCount > 0) {
            throw new IllegalStateException(
                    "Cannot delete user with existing reports. Deactivate instead."
            );
        }

        userRepository.delete(user);
    }
    // ================= Helpers =================

    @SuppressWarnings("unchecked")
    private User provisionUser(Jwt jwt, String keycloakId) {
        String email = jwt.getClaimAsString("email");
        String name = jwt.getClaimAsString("name");

        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        List<String> roles = realmAccess != null
                ? (List<String>) realmAccess.get("roles")
                : List.of();

        UserRole role = roles.contains("MANAGER") ? UserRole.MANAGER : UserRole.TEAM_MEMBER;

        User newUser = User.builder()
                .keycloakId(keycloakId)
                .email(email)
                .name(name != null ? name : email)
                .role(role)
                .build();

        return userRepository.save(newUser);
    }

    private UserDto toDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}