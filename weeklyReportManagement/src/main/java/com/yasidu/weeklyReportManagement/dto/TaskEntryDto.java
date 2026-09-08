package com.yasidu.weeklyReportManagement.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskEntryDto {
    private Long id;

    @NotBlank(message = "Task name is required")
    private String taskName;

    private String priority;

    @Min(0) @Max(100)
    private Integer plannedPct;

    private String taskType;

    @Min(0) @Max(100)
    private Integer actualPct;

    private String status;
    private Float timePlanned;
    private Float timeSpent;
    private String deliverable;
}