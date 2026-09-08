package com.yasidu.weeklyReportManagement.service.impl;

import com.yasidu.weeklyReportManagement.dto.ReportResponseDto;
import com.yasidu.weeklyReportManagement.dto.ReviewCommentDto;
import com.yasidu.weeklyReportManagement.dto.ReviewRequestDto;
import com.yasidu.weeklyReportManagement.entity.Report;
import com.yasidu.weeklyReportManagement.entity.ReportVersion;
import com.yasidu.weeklyReportManagement.entity.ReviewComment;
import com.yasidu.weeklyReportManagement.entity.User;
import com.yasidu.weeklyReportManagement.enums.ReportStatus;
import com.yasidu.weeklyReportManagement.enums.ReviewAction;
import com.yasidu.weeklyReportManagement.repository.ReportRepository;
import com.yasidu.weeklyReportManagement.repository.ReportVersionRepository;
import com.yasidu.weeklyReportManagement.repository.ReviewCommentRepository;
import com.yasidu.weeklyReportManagement.service.ReportService;
import com.yasidu.weeklyReportManagement.service.ReviewService;
import com.yasidu.weeklyReportManagement.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReportRepository reportRepository;
    private final ReportVersionRepository reportVersionRepository;   // ← මේ field එක add කරන්න ඕන
    private final ReviewCommentRepository reviewCommentRepository;
    private final UserService userService;
    private final ReportService reportService;

    @Override
    @Transactional
    public ReportResponseDto reviewReport(Long reportId, ReviewRequestDto dto) {
        User manager = userService.getCurrentUser();

        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new EntityNotFoundException("Report not found: " + reportId));

        if (report.getStatus() != ReportStatus.SUBMITTED) {
            throw new IllegalStateException("Only submitted reports can be reviewed");
        }

        if (dto.getAction() == ReviewAction.CHANGES_REQUESTED &&
                (dto.getComment() == null || dto.getComment().isBlank())) {
            throw new IllegalArgumentException("A comment is required when requesting changes");
        }

        // Link this comment to whichever version was current at review time
        ReportVersion latestVersion = reportVersionRepository
                .findByReportIdOrderByVersionNumberDesc(report.getId())
                .stream()
                .findFirst()
                .orElse(null);

        ReviewComment comment = ReviewComment.builder()
                .report(report)
                .manager(manager)
                .action(dto.getAction())
                .comment(dto.getComment())
                .reportVersion(latestVersion)
                .build();
        reviewCommentRepository.save(comment);

        report.setStatus(dto.getAction() == ReviewAction.APPROVED
                ? ReportStatus.APPROVED
                : ReportStatus.NEEDS_CORRECTION);
        reportRepository.save(report);

        return reportService.getReportById(reportId);
    }

    @Override
    public List<ReviewCommentDto> getCommentHistory(Long reportId) {
        return reviewCommentRepository.findByReportIdOrderByCreatedAtDesc(reportId).stream()
                .map(c -> ReviewCommentDto.builder()
                        .id(c.getId())
                        .managerName(c.getManager().getName())
                        .action(c.getAction())
                        .comment(c.getComment())
                        .createdAt(c.getCreatedAt())
                        .reportVersionNumber(
                                c.getReportVersion() != null ? c.getReportVersion().getVersionNumber() : null
                        )
                        .build())
                .collect(Collectors.toList());
    }
}