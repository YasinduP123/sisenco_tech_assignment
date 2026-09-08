package com.yasidu.weeklyReportManagement.dto;

import com.yasidu.weeklyReportManagement.enums.ReportStatus;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MemberStatusDto {
    private Long userId;
    private String userName;
    private ReportStatus status; // null/"NOT_STARTED" if no report exists yet
}