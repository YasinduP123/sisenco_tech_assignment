package com.yasidu.weeklyReportManagement.dto;

import com.yasidu.weeklyReportManagement.enums.ReviewAction;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewRequestDto {
    @NotNull(message = "Action is required")
    private ReviewAction action; // APPROVED or CHANGES_REQUESTED

    private String comment; // required when action = CHANGES_REQUESTED (validated in service)
}