package com.yasidu.weeklyReportManagement.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TaskTypeTimeDto {
    private String taskType;
    private Float totalHours;
}