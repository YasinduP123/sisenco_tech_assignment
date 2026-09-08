package com.yasidu.weeklyReportManagement.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DashboardSummaryDto {
    private Long totalSubmittedThisWeek;
    private Double complianceRatePct;
    private Long needsCorrectionCount;
    private Long openBlockersCount;
}