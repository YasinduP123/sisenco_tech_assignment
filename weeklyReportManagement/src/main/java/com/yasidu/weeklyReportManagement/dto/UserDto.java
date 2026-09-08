package com.yasidu.weeklyReportManagement.dto;

import com.yasidu.weeklyReportManagement.enums.UserRole;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {
    private Long id;
    private String name;
    private String email;
    private UserRole role;
}
