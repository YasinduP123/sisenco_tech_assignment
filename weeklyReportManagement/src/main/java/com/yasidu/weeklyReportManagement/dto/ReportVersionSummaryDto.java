package com.yasidu.weeklyReportManagement.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportVersionSummaryDto {
    private Long id;
    private Integer versionNumber;
    private LocalDateTime createdAt;
    private String contentSnapshot; // raw JSON string, frontend parses on demand
}