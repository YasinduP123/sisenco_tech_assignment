package com.yasidu.weeklyReportManagement.entity;

import com.yasidu.weeklyReportManagement.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Keycloak's "sub" claim — links this row to the Keycloak identity
    @Column(name = "keycloak_id", unique = true, nullable = false)
    private String keycloakId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 150)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // A team member submits many reports
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Report> reports;

    // A manager writes many review comments
    @OneToMany(mappedBy = "manager")
    private List<ReviewComment> reviewComments;
}