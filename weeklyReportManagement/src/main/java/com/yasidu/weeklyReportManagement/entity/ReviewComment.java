package com.yasidu.weeklyReportManagement.entity;

import com.yasidu.weeklyReportManagement.enums.ReviewAction;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "review_comments")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ReviewComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id", nullable = false)
    private Report report;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id", nullable = false)
    private User manager;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReviewAction action;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // Which version of the report this comment was made against (for the version-history bonus)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_version_id")
    private ReportVersion reportVersion;
}