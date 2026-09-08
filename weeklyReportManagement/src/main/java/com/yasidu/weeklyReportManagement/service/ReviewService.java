package com.yasidu.weeklyReportManagement.service;


import com.yasidu.weeklyReportManagement.dto.ReportResponseDto;
import com.yasidu.weeklyReportManagement.dto.ReviewCommentDto;
import com.yasidu.weeklyReportManagement.dto.ReviewRequestDto;

import java.util.List;

public interface ReviewService {

    /**
     * Manager-only: reviews a SUBMITTED report.
     * - action = APPROVED  -> report status becomes APPROVED
     * - action = CHANGES_REQUESTED -> report status becomes NEEDS_CORRECTION,
     *   comment is required and stored against the report's current version.
     * Throws IllegalStateException if the report is not currently SUBMITTED.
     */
    ReportResponseDto reviewReport(Long reportId, ReviewRequestDto dto);

    /**
     * Returns the full comment history for a report, newest first (bonus feature —
     * keeping every past review comment rather than only the latest).
     */
    List<ReviewCommentDto> getCommentHistory(Long reportId);
}