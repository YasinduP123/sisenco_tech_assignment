package com.yasidu.weeklyReportManagement.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "task_entries")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class TaskEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id", nullable = false)
    private Report report;

    @Column(name = "task_name", nullable = false, length = 200)
    private String taskName;

    // TaskEntry.java එකට add කරන්න
    @Column(name = "task_type", length = 30)
    private String taskType; // e.g. "Development", "Testing", "Meetings", "Documentation"

    @Column(length = 20)
    private String priority;

    @Column(name = "planned_pct")
    private Integer plannedPct;

    @Column(name = "actual_pct")
    private Integer actualPct;

    @Column(length = 30)
    private String status;

    @Column(name = "time_planned")
    private Float timePlanned;

    @Column(name = "time_spent")
    private Float timeSpent;

    @Column(columnDefinition = "TEXT")
    private String deliverable;
}