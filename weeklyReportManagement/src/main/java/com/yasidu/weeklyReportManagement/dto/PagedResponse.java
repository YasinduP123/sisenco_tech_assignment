package com.yasidu.weeklyReportManagement.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PagedResponse<T> {
    private java.util.List<T> content;
    private Integer page;
    private Integer size;
    private Long totalElements;
    private Integer totalPages;
}