package com.yasidu.weeklyReportManagement.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportRequestDto {
    // used for create/update payloads
    private Long projectId;

    @NotNull(message = "Week start is required")
    private LocalDate weekStart;

    @NotNull(message = "Week end is required")
    private LocalDate weekEnd;

    private String nextWeekPlan;
    private String keyBlocker;
    private String keyAchievement;
    private String notes;

    @Valid
    private List<TaskEntryDto> taskEntries;
}