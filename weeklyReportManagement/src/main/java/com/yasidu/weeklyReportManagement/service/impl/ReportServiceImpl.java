package com.yasidu.weeklyReportManagement.service.impl;

import com.yasidu.weeklyReportManagement.dto.*;
import com.yasidu.weeklyReportManagement.entity.*;
import com.yasidu.weeklyReportManagement.enums.ReportStatus;
import com.yasidu.weeklyReportManagement.repository.*;
import com.yasidu.weeklyReportManagement.service.ReportService;
import com.yasidu.weeklyReportManagement.util.CurrentUserResolver;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;
    private final ProjectRepository projectRepository;
    private final TaskEntryRepository taskEntryRepository;
    private final ReportVersionRepository reportVersionRepository;
    private final ReviewCommentRepository reviewCommentRepository;
    private final CurrentUserResolver currentUserResolver;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // ---------- CREATE DRAFT ----------
    @Transactional
    public ReportResponseDto createDraft(ReportRequestDto dto) {
        User currentUser = currentUserResolver.resolve();

        Report report = Report.builder()
                .user(currentUser)
                .project(dto.getProjectId() != null ? getProject(dto.getProjectId()) : null)
                .weekStart(dto.getWeekStart())
                .weekEnd(dto.getWeekEnd())
                .status(ReportStatus.DRAFT)
                .nextWeekPlan(dto.getNextWeekPlan())
                .keyBlocker(dto.getKeyBlocker())
                .keyAchievement(dto.getKeyAchievement())
                .notes(dto.getNotes())
                .build();

        attachTaskEntries(report, dto.getTaskEntries());

        Report saved = reportRepository.save(report);
        return toResponseDto(saved);
    }

    // ---------- EDIT (only allowed in DRAFT or NEEDS_CORRECTION) ----------
    @Transactional
    public ReportResponseDto updateReport(Long reportId, ReportRequestDto dto) {
        Report report = getOwnedReportForEdit(reportId);

        if (report.getStatus() == ReportStatus.NEEDS_CORRECTION) {
            snapshotCurrentVersion(report); // preserve pre-edit content for history
        }

        report.setProject(dto.getProjectId() != null ? getProject(dto.getProjectId()) : null);
        report.setWeekStart(dto.getWeekStart());
        report.setWeekEnd(dto.getWeekEnd());
        report.setNextWeekPlan(dto.getNextWeekPlan());
        report.setKeyBlocker(dto.getKeyBlocker());
        report.setKeyAchievement(dto.getKeyAchievement());
        report.setNotes(dto.getNotes());

        report.getTaskEntries().clear();
        taskEntryRepository.flush();
        attachTaskEntries(report, dto.getTaskEntries());

        return toResponseDto(reportRepository.save(report));
    }

    // ---------- SUBMIT (DRAFT -> SUBMITTED, or NEEDS_CORRECTION -> SUBMITTED) ----------
    @Transactional
    public ReportResponseDto submitReport(Long reportId) {
        Report report = getOwnedReportForEdit(reportId);

        if (report.getStatus() != ReportStatus.DRAFT && report.getStatus() != ReportStatus.NEEDS_CORRECTION) {
            throw new IllegalStateException("Only draft or needs-correction reports can be submitted");
        }

        report.setStatus(ReportStatus.SUBMITTED);
        report.setSubmittedAt(LocalDateTime.now());
        return toResponseDto(reportRepository.save(report));
    }

    @Override
    public void deleteDraft(Long reportId) {

    }

    // ---------- GET SINGLE REPORT (ownership enforced for team members) ----------
    public ReportResponseDto getReportById(Long reportId) {
        User currentUser = currentUserResolver.resolve();
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new EntityNotFoundException("Report not found: " + reportId));

        boolean isOwner = report.getUser().getId().equals(currentUser.getId());
        boolean isManager = currentUser.getRole().name().equals("MANAGER");

        if (!isOwner && !isManager) {
            throw new AccessDeniedException("You cannot access another user's report");
        }
        return toResponseDto(report);
    }

    // ---------- LIST OWN REPORTS (team member's history page) ----------
    public PagedResponse<ReportResponseDto> getMyReports(int page, int size) {
        User currentUser = currentUserResolver.resolve();
        Pageable pageable = PageRequest.of(page, size);
        Page<Report> result = reportRepository.findByUserId(currentUser.getId(), pageable);
        return toPagedResponse(result);
    }

    // ---------- MANAGER: FILTERED LIST ACROSS TEAM ----------
    public PagedResponse<ReportResponseDto> searchReports(
            Long userId, Long projectId, ReportStatus status,
            LocalDate weekStart, LocalDate weekEnd, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Report> result = reportRepository.searchReports(userId, projectId, status, weekStart, weekEnd, pageable);
        return toPagedResponse(result);
    }

    @Override
    public List<ReportResponseDto> getTeamReportsForWeek(LocalDate weekStart) {
        return List.of();
    }

    // ---------- VERSION HISTORY ----------
    public List<ReportVersionSummaryDto> getVersionHistory(Long reportId) {
        return reportVersionRepository.findByReportIdOrderByVersionNumberDesc(reportId).stream()
                .map(v -> ReportVersionSummaryDto.builder()
                        .id(v.getId())
                        .versionNumber(v.getVersionNumber())
                        .createdAt(v.getCreatedAt())
                        .contentSnapshot(v.getContentSnapshot())
                        .build())
                .collect(Collectors.toList());
    }

    // ================= Helpers =================

    private Report getOwnedReportForEdit(Long reportId) {
        User currentUser = currentUserResolver.resolve();
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new EntityNotFoundException("Report not found: " + reportId));

        if (!report.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You can only edit your own reports");
        }
        if (report.getStatus() != ReportStatus.DRAFT && report.getStatus() != ReportStatus.NEEDS_CORRECTION) {
            throw new IllegalStateException("Report is not editable in its current status");
        }
        return report;
    }

    private Project getProject(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project not found: " + projectId));
    }

    private void attachTaskEntries(Report report, List<TaskEntryDto> taskDtos) {
        if (taskDtos == null) return;
        for (TaskEntryDto t : taskDtos) {
            TaskEntry entry = TaskEntry.builder()
                    .report(report)
                    .taskName(t.getTaskName())
                    .priority(t.getPriority())
                    .plannedPct(t.getPlannedPct())
                    .actualPct(t.getActualPct())
                    .status(t.getStatus())
                    .timePlanned(t.getTimePlanned())
                    .timeSpent(t.getTimeSpent())
                    .deliverable(t.getDeliverable())
                    .build();
            report.getTaskEntries().add(entry);
        }
    }

    // Snapshot the report's CURRENT (pre-edit) state as JSON before it gets overwritten
    private void snapshotCurrentVersion(Report report) {
        try {
            int nextVersionNumber = reportVersionRepository
                    .findByReportIdOrderByVersionNumberDesc(report.getId())
                    .stream().findFirst().map(v -> v.getVersionNumber() + 1).orElse(1);

            ReportResponseDto snapshotContent = toResponseDto(report);
            String json = objectMapper.writeValueAsString(snapshotContent);

            ReportVersion version = ReportVersion.builder()
                    .report(report)
                    .versionNumber(nextVersionNumber)
                    .contentSnapshot(json)
                    .build();

            reportVersionRepository.save(version);
        } catch (Exception e) {
            throw new RuntimeException("Failed to snapshot report version", e);
        }
    }

    private ReportResponseDto toResponseDto(Report r) {
        String latestComment = reviewCommentRepository
                .findFirstByReportIdOrderByCreatedAtDesc(r.getId())
                .map(ReviewComment::getComment)
                .orElse(null);

        List<TaskEntryDto> tasks = r.getTaskEntries() == null ? List.of() :
                r.getTaskEntries().stream().map(t -> TaskEntryDto.builder()
                        .id(t.getId())
                        .taskName(t.getTaskName())
                        .priority(t.getPriority())
                        .plannedPct(t.getPlannedPct())
                        .actualPct(t.getActualPct())
                        .status(t.getStatus())
                        .timePlanned(t.getTimePlanned())
                        .timeSpent(t.getTimeSpent())
                        .deliverable(t.getDeliverable())
                        .build()).collect(Collectors.toList());

        return ReportResponseDto.builder()
                .id(r.getId())
                .userId(r.getUser().getId())
                .userName(r.getUser().getName())
                .projectId(r.getProject() != null ? r.getProject().getId() : null)
                .projectName(r.getProject() != null ? r.getProject().getName() : null)
                .weekStart(r.getWeekStart())
                .weekEnd(r.getWeekEnd())
                .status(r.getStatus())
                .nextWeekPlan(r.getNextWeekPlan())
                .keyBlocker(r.getKeyBlocker())
                .keyAchievement(r.getKeyAchievement())
                .notes(r.getNotes())
                .submittedAt(r.getSubmittedAt())
                .updatedAt(r.getUpdatedAt())
                .taskEntries(tasks)
                .latestComment(latestComment)
                .build();
    }

    private PagedResponse<ReportResponseDto> toPagedResponse(Page<Report> page) {
        PagedResponse<ReportResponseDto> response = new PagedResponse<>();
        response.setContent(page.getContent().stream().map(this::toResponseDto).collect(Collectors.toList()));
        response.setPage(page.getNumber());
        response.setSize(page.getSize());
        response.setTotalElements(page.getTotalElements());
        response.setTotalPages(page.getTotalPages());
        return response;
    }
}