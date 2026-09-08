package com.yasidu.weeklyReportManagement.repository;

import com.yasidu.weeklyReportManagement.entity.ReportVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReportVersionRepository extends JpaRepository<ReportVersion, Long> {
    List<ReportVersion> findByReportIdOrderByVersionNumberDesc(Long reportId);
}