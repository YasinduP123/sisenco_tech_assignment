package com.yasidu.weeklyReportManagement.util;

import com.yasidu.weeklyReportManagement.entity.User;
import com.yasidu.weeklyReportManagement.enums.UserRole;
import com.yasidu.weeklyReportManagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CurrentUserResolver {

    private final UserRepository userRepository;

    public User resolve() {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String keycloakId = jwt.getSubject();

        return userRepository.findByKeycloakId(keycloakId)
                .orElseGet(() -> provisionUser(jwt, keycloakId));
    }

    // First-time login: auto-create local profile from Keycloak token claims
    private User provisionUser(Jwt jwt, String keycloakId) {
        String email = jwt.getClaimAsString("email");
        String name = jwt.getClaimAsString("name");

        java.util.List<String> roles =
                (java.util.List<String>) ((java.util.Map<String, Object>) jwt.getClaim("realm_access")).get("roles");

        UserRole role = roles.contains("MANAGER") ? UserRole.MANAGER : UserRole.TEAM_MEMBER;

        User newUser = User.builder()
                .keycloakId(keycloakId)
                .email(email)
                .name(name != null ? name : email)
                .role(role)
                .build();

        return userRepository.save(newUser);
    }
}
