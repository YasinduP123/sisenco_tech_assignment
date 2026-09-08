package com.yasidu.weeklyReportManagement.service;

import com.yasidu.weeklyReportManagement.dto.*;

import java.time.LocalDate;
import java.util.List;

public interface DashboardService {
    // ---------- Summary metrics ----------
    DashboardSummaryDto getSummaryMetrics(LocalDate weekStart);

    // ---------- Tasks completed trend (last 6 weeks, team-wide or per user) ----------
    TasksTrendDto getTasksCompletedTrend(Long userId, LocalDate from, LocalDate to);

    // ---------- Status by member for a given week ----------
    List<MemberStatusDto> getStatusByMember(LocalDate weekStart);

    // ---------- Workload by project ----------
    List<ProjectWorkloadDto> getWorkloadByProject(LocalDate from, LocalDate to);

    // ---------- Time spent by task type ----------
    List<TaskTypeTimeDto> getTimeByTaskType(LocalDate from, LocalDate to);

    // ---------- Recent activity feed ----------
    List<ActivityFeedItemDto> getRecentActivity(int limit);
}
