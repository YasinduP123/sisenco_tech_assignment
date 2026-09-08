package com.yasidu.weeklyReportManagement.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectDto {
    private Long id;
    private String name;
    private String description;
    private Boolean active;
}
