package com.yasidu.weeklyReportManagement.dto;

import com.yasidu.weeklyReportManagement.enums.ReportStatus;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportResponseDto {
    private Long id;
    private Long userId;
    private String userName;
    private Long projectId;
    private String projectName;
    private LocalDate weekStart;
    private LocalDate weekEnd;
    private ReportStatus status;
    private String nextWeekPlan;
    private String keyBlocker;
    private String keyAchievement;
    private String notes;
    private LocalDateTime submittedAt;
    private LocalDateTime updatedAt;
    private List<TaskEntryDto> taskEntries;
    private String latestComment; // most recent manager comment, if any
}