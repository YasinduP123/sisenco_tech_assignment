package com.yasidu.weeklyReportManagement.service.impl;

import com.yasidu.weeklyReportManagement.dto.*;
import com.yasidu.weeklyReportManagement.entity.Report;
import com.yasidu.weeklyReportManagement.entity.TaskEntry;
import com.yasidu.weeklyReportManagement.entity.User;
import com.yasidu.weeklyReportManagement.enums.ReportStatus;
import com.yasidu.weeklyReportManagement.enums.UserRole;
import com.yasidu.weeklyReportManagement.repository.ReportRepository;
import com.yasidu.weeklyReportManagement.repository.ReviewCommentRepository;
import com.yasidu.weeklyReportManagement.repository.UserRepository;
import com.yasidu.weeklyReportManagement.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final ReviewCommentRepository reviewCommentRepository;

    // ---------- Summary metrics ----------
    @Override
    public DashboardSummaryDto getSummaryMetrics(LocalDate weekStart) {
        List<Report> weekReports = reportRepository.findByWeekStart(weekStart);

        long submitted = weekReports.stream()
                .filter(r -> r.getStatus() != ReportStatus.DRAFT)
                .count();

        // Count DISTINCT users who submitted — not total report rows.
        // A user with 2 draft/submitted reports for the same week (e.g. from
        // duplicate test data) should still count as 1 person for compliance %.
        long submittedUsers = weekReports.stream()
                .filter(r -> r.getStatus() != ReportStatus.DRAFT)
                .map(r -> r.getUser().getId())
                .distinct()
                .count();

        long totalTeamMembers = userRepository.findByRole(UserRole.TEAM_MEMBER).size();

        // Guard against division by zero when there are no team members yet
        double complianceRate = totalTeamMembers == 0
                ? 0.0
                : (submittedUsers * 100.0) / totalTeamMembers;

        long needsCorrection = reportRepository.countByStatus(ReportStatus.NEEDS_CORRECTION);

        long openBlockers = weekReports.stream()
                .filter(r -> r.getKeyBlocker() != null && !r.getKeyBlocker().isBlank())
                .count();

        return DashboardSummaryDto.builder()
                .totalSubmittedThisWeek(submitted)
                .complianceRatePct(complianceRate)
                .needsCorrectionCount(needsCorrection)
                .openBlockersCount(openBlockers)
                .build();
    }

    // ---------- Tasks completed trend (last 6 weeks, team-wide or per user) ----------
    @Override
    public TasksTrendDto getTasksCompletedTrend(Long userId, LocalDate from, LocalDate to) {
        List<Report> reports = reportRepository.findAllInDateRange(from, to);

        if (userId != null) {
            reports = reports.stream()
                    .filter(r -> r.getUser().getId().equals(userId))
                    .toList();
        }

        // Group by weekStart, count tasks marked as "Done"/"Completed" (case-insensitive)
        Map<LocalDate, Long> countsByWeek = new TreeMap<>();
        for (Report r : reports) {
            long completedTasks = r.getTaskEntries().stream()
                    .filter(t -> t.getStatus() != null &&
                            (t.getStatus().equalsIgnoreCase("Done") || t.getStatus().equalsIgnoreCase("Completed")))
                    .count();
            countsByWeek.merge(r.getWeekStart(), completedTasks, Long::sum);
        }

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM d");
        List<String> labels = new ArrayList<>();
        List<Integer> counts = new ArrayList<>();
        for (Map.Entry<LocalDate, Long> entry : countsByWeek.entrySet()) {
            labels.add(entry.getKey().format(fmt));
            counts.add(entry.getValue().intValue());
        }

        return TasksTrendDto.builder()
                .weekLabels(labels)
                .completedCounts(counts)
                .build();
    }

    // ---------- Status by member for a given week ----------
    @Override
    public List<MemberStatusDto> getStatusByMember(LocalDate weekStart) {
        List<User> teamMembers = userRepository.findByRole(UserRole.TEAM_MEMBER);
        List<Report> weekReports = reportRepository.findByWeekStart(weekStart);

        Map<Long, ReportStatus> statusByUserId = weekReports.stream()
                .collect(Collectors.toMap(r -> r.getUser().getId(), Report::getStatus, (a, b) -> b));

        return teamMembers.stream()
                .map(u -> MemberStatusDto.builder()
                        .userId(u.getId())
                        .userName(u.getName())
                        .status(statusByUserId.get(u.getId())) // null => "not started" (frontend shows this)
                        .build())
                .collect(Collectors.toList());
    }

    // ---------- Workload by project ----------
    @Override
    public List<ProjectWorkloadDto> getWorkloadByProject(LocalDate from, LocalDate to) {
        List<Report> reports = reportRepository.findAllInDateRange(from, to);

        Map<Long, String> projectNames = new HashMap<>();
        Map<Long, Long> taskCounts = new HashMap<>();

        for (Report r : reports) {
            if (r.getProject() == null) continue;
            Long pid = r.getProject().getId();
            projectNames.putIfAbsent(pid, r.getProject().getName());
            taskCounts.merge(pid, (long) r.getTaskEntries().size(), Long::sum);
        }

        return taskCounts.entrySet().stream()
                .map(e -> ProjectWorkloadDto.builder()
                        .projectId(e.getKey())
                        .projectName(projectNames.get(e.getKey()))
                        .taskCount(e.getValue())
                        .build())
                .collect(Collectors.toList());
    }

    // ---------- Time spent by task type ----------
    @Override
    public List<TaskTypeTimeDto> getTimeByTaskType(LocalDate from, LocalDate to) {
        List<Report> reports = reportRepository.findAllInDateRange(from, to);

        Map<String, Float> hoursByType = new LinkedHashMap<>();
        for (Report r : reports) {
            for (TaskEntry t : r.getTaskEntries()) {
                String type = t.getTaskType() != null ? t.getTaskType() : "Uncategorized";
                float hours = t.getTimeSpent() != null ? t.getTimeSpent() : 0f;
                hoursByType.merge(type, hours, Float::sum);
            }
        }

        return hoursByType.entrySet().stream()
                .map(e -> TaskTypeTimeDto.builder()
                        .taskType(e.getKey())
                        .totalHours(e.getValue())
                        .build())
                .collect(Collectors.toList());
    }

    // ---------- Recent activity feed ----------
    @Override
    public List<ActivityFeedItemDto> getRecentActivity(int limit) {
        List<ActivityFeedItemDto> items = new ArrayList<>();

        // Recent submissions
        reportRepository.findAll().stream()
                .filter(r -> r.getSubmittedAt() != null)
                .sorted(Comparator.comparing(Report::getSubmittedAt).reversed())
                .limit(limit)
                .forEach(r -> items.add(ActivityFeedItemDto.builder()
                        .type("SUBMITTED")
                        .userName(r.getUser().getName())
                        .reportId(r.getId())
                        .timestamp(r.getSubmittedAt())
                        .summary(r.getUser().getName() + " submitted their report for " + r.getWeekStart())
                        .build()));

        // Recent review actions
        reviewCommentRepository.findAll().stream()
                .sorted(Comparator.comparing(com.yasidu.weeklyReportManagement.entity.ReviewComment::getCreatedAt).reversed())
                .limit(limit)
                .forEach(c -> items.add(ActivityFeedItemDto.builder()
                        .type(c.getAction().name())
                        .userName(c.getManager().getName())
                        .reportId(c.getReport().getId())
                        .timestamp(c.getCreatedAt())
                        .summary(c.getManager().getName() + " " +
                                (c.getAction().name().equals("APPROVED") ? "approved" : "requested changes on") +
                                " " + c.getReport().getUser().getName() + "'s report")
                        .build()));

        return items.stream()
                .sorted(Comparator.comparing(ActivityFeedItemDto::getTimestamp).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }
}