package com.yasidu.weeklyReportManagement.controller;

import com.yasidu.weeklyReportManagement.dto.*;
import com.yasidu.weeklyReportManagement.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MANAGER')")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryDto> getSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart) {
        return ResponseEntity.ok(dashboardService.getSummaryMetrics(weekStart));
    }

    @GetMapping("/tasks-trend")
    public ResponseEntity<TasksTrendDto> getTasksTrend(
            @RequestParam(required = false) Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(dashboardService.getTasksCompletedTrend(userId, from, to));
    }

    @GetMapping("/status-by-member")
    public ResponseEntity<List<MemberStatusDto>> getStatusByMember(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart) {
        return ResponseEntity.ok(dashboardService.getStatusByMember(weekStart));
    }

    @GetMapping("/workload-by-project")
    public ResponseEntity<List<ProjectWorkloadDto>> getWorkloadByProject(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(dashboardService.getWorkloadByProject(from, to));
    }

    @GetMapping("/time-by-task-type")
    public ResponseEntity<List<TaskTypeTimeDto>> getTimeByTaskType(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(dashboardService.getTimeByTaskType(from, to));
    }

    @GetMapping("/recent-activity")
    public ResponseEntity<List<ActivityFeedItemDto>> getRecentActivity(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(dashboardService.getRecentActivity(limit));
    }
}