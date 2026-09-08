package com.yasidu.weeklyReportManagement.dto;

import lombok.*;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TasksTrendDto {
    private List<String> weekLabels;
    private List<Integer> completedCounts;
}
