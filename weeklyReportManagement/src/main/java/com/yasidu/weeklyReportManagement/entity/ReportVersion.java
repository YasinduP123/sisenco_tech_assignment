package com.yasidu.weeklyReportManagement.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "report_versions")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ReportVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id", nullable = false)
    private Report report;

    @Column(name = "version_number", nullable = false)
    private Integer versionNumber;

    // Full snapshot of report content (tasks, blockers, achievements etc.) as JSON text
    @Lob
    @Column(name = "content_snapshot", columnDefinition = "JSON")
    private String contentSnapshot;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}