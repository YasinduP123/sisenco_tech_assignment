package com.yasidu.weeklyReportManagement.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProjectWorkloadDto {
    private Long projectId;
    private String projectName;
    private Long taskCount;
}