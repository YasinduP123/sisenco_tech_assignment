package com.yasidu.weeklyReportManagement.controller;

import com.yasidu.weeklyReportManagement.dto.PagedResponse;
import com.yasidu.weeklyReportManagement.dto.ReportRequestDto;
import com.yasidu.weeklyReportManagement.dto.ReportResponseDto;
import com.yasidu.weeklyReportManagement.dto.ReportVersionSummaryDto;
import com.yasidu.weeklyReportManagement.enums.ReportStatus;
import com.yasidu.weeklyReportManagement.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    // ---- Team member endpoints ----

    @PostMapping
    public ResponseEntity<ReportResponseDto> createDraft(@Valid @RequestBody ReportRequestDto dto) {
        return ResponseEntity.ok(reportService.createDraft(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReportResponseDto> update(
            @PathVariable Long id, @Valid @RequestBody ReportRequestDto dto) {
        return ResponseEntity.ok(reportService.updateReport(id, dto));
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<ReportResponseDto> submit(@PathVariable Long id) {
        return ResponseEntity.ok(reportService.submitReport(id));
    }

    @GetMapping("/my")
    public ResponseEntity<PagedResponse<ReportResponseDto>> getMyReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(reportService.getMyReports(page, size));
    }

    // ---- Shared endpoint (ownership/role checked inside service) ----

    @GetMapping("/{id}")
    public ResponseEntity<ReportResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(reportService.getReportById(id));
    }

    @GetMapping("/{id}/versions")
    public ResponseEntity<List<ReportVersionSummaryDto>> getVersions(@PathVariable Long id) {
        return ResponseEntity.ok(reportService.getVersionHistory(id));
    }

    // ---- Manager-only endpoint ----

    @GetMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<PagedResponse<ReportResponseDto>> searchReports(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) ReportStatus status,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate weekStart,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate weekEnd,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(
                reportService.searchReports(userId, projectId, status, weekStart, weekEnd, page, size)
        );
    }
}