package com.yasidu.weeklyReportManagement.service;


import com.yasidu.weeklyReportManagement.dto.PagedResponse;
import com.yasidu.weeklyReportManagement.dto.ReportRequestDto;
import com.yasidu.weeklyReportManagement.dto.ReportResponseDto;
import com.yasidu.weeklyReportManagement.dto.ReportVersionSummaryDto;
import com.yasidu.weeklyReportManagement.enums.ReportStatus;

import java.time.LocalDate;
import java.util.List;

public interface ReportService {

    // ---------- Team member operations ----------

    /**
     * Creates a new report in DRAFT status for the current user.
     */
    ReportResponseDto createDraft(ReportRequestDto dto);

    /**
     * Updates an existing report. Only allowed when the report belongs to the
     * current user AND its status is DRAFT or NEEDS_CORRECTION.
     * If status is NEEDS_CORRECTION, the pre-edit content must be snapshotted
     * into report_versions before applying changes.
     */
    ReportResponseDto updateReport(Long reportId, ReportRequestDto dto);

    /**
     * Transitions a report from DRAFT or NEEDS_CORRECTION to SUBMITTED.
     * Sets submittedAt timestamp.
     */
    ReportResponseDto submitReport(Long reportId);

    /**
     * Deletes a report that is still in DRAFT status (optional convenience method).
     */
    void deleteDraft(Long reportId);

    /**
     * Returns the current user's own reports, paginated, newest week first.
     */
    PagedResponse<ReportResponseDto> getMyReports(int page, int size);

    // ---------- Shared operations (ownership/role enforced internally) ----------

    /**
     * Fetches a single report by id. Team members may only fetch their own;
     * managers may fetch any. Throws AccessDeniedException otherwise.
     */
    ReportResponseDto getReportById(Long reportId);

    /**
     * Returns the version history (past snapshots) for a report, newest first.
     */
    List<ReportVersionSummaryDto> getVersionHistory(Long reportId);

    // ---------- Manager-only operations ----------

    /**
     * Searches/filters reports across the whole team.
     * Any parameter may be null to mean "no filter on this field".
     */
    PagedResponse<ReportResponseDto> searchReports(
            Long userId,
            Long projectId,
            ReportStatus status,
            LocalDate weekStart,
            LocalDate weekEnd,
            int page,
            int size
    );

    /**
     * Returns one report per team member for a given week (used by the
     * manager dashboard's weekly team view). Members with no report for
     * that week should appear with a null/placeholder entry.
     */
    List<ReportResponseDto> getTeamReportsForWeek(LocalDate weekStart);
}