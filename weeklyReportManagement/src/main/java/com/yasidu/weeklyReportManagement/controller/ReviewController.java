package com.yasidu.weeklyReportManagement.controller;


import com.yasidu.weeklyReportManagement.dto.ReportResponseDto;
import com.yasidu.weeklyReportManagement.dto.ReviewCommentDto;
import com.yasidu.weeklyReportManagement.dto.ReviewRequestDto;
import com.yasidu.weeklyReportManagement.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/{id}/review")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ReportResponseDto> review(
            @PathVariable Long id, @Valid @RequestBody ReviewRequestDto dto) {
        return ResponseEntity.ok(reviewService.reviewReport(id, dto));
    }

    // Was missing before — the frontend's getCommentHistory() call needs this.
    // Available to both roles: team member sees comments on their own report
    // (ownership already enforced when they fetched the report itself),
    // manager can see comments on any report they're reviewing.
    @GetMapping("/{id}/comments")
    public ResponseEntity<List<ReviewCommentDto>> getComments(@PathVariable Long id) {
        return ResponseEntity.ok(reviewService.getCommentHistory(id));
    }
}

