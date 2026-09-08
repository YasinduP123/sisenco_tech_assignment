package com.yasidu.weeklyReportManagement.dto;

import com.yasidu.weeklyReportManagement.enums.ReviewAction;
import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ReviewCommentDto {
    private Long id;
    private String managerName;
    private ReviewAction action;
    private String comment;
    private LocalDateTime createdAt;
    private Integer reportVersionNumber; // which version this comment was made against
}