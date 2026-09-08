package com.yasidu.weeklyReportManagement.entity;

import com.yasidu.weeklyReportManagement.enums.ReportStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "reports")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "week_start", nullable = false)
    private LocalDate weekStart;

    @Column(name = "week_end", nullable = false)
    private LocalDate weekEnd;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ReportStatus status = ReportStatus.DRAFT;

    @Column(name = "next_week_plan", columnDefinition = "TEXT")
    private String nextWeekPlan;

    @Column(name = "key_blocker", columnDefinition = "TEXT")
    private String keyBlocker;

    @Column(name = "key_achievement", columnDefinition = "TEXT")
    private String keyAchievement;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // One report has many task rows
    @OneToMany(mappedBy = "report", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TaskEntry> taskEntries = new java.util.ArrayList<>();

    // One report accumulates version snapshots over its correction cycles
    @OneToMany(mappedBy = "report", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ReportVersion> versions = new java.util.ArrayList<>();

    // One report can receive multiple review comments over time (bonus: history)
    @OneToMany(mappedBy = "report", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ReviewComment> reviewComments = new java.util.ArrayList<>();
}