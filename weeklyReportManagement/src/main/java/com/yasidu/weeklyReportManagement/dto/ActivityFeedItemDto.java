package com.yasidu.weeklyReportManagement.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ActivityFeedItemDto {
    private String type;        // "SUBMITTED", "APPROVED", "CHANGES_REQUESTED"
    private String userName;
    private Long reportId;
    private LocalDateTime timestamp;
    private String summary;     // short human-readable description
}