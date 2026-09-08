package com.yasidu.weeklyReportManagement.repository;

import com.yasidu.weeklyReportManagement.entity.Report;
import com.yasidu.weeklyReportManagement.enums.ReportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {

    Page<Report> findByUserId(Long userId, Pageable pageable);

    @Query("SELECT r FROM Report r WHERE " +
            "(:userId IS NULL OR r.user.id = :userId) AND " +
            "(:projectId IS NULL OR r.project.id = :projectId) AND " +
            "(:status IS NULL OR r.status = :status) AND " +
            "(:weekStart IS NULL OR r.weekStart >= :weekStart) AND " +
            "(:weekEnd IS NULL OR r.weekEnd <= :weekEnd)")
    Page<Report> searchReports(
            @Param("userId") Long userId,
            @Param("projectId") Long projectId,
            @Param("status") ReportStatus status,
            @Param("weekStart") LocalDate weekStart,
            @Param("weekEnd") LocalDate weekEnd,
            Pageable pageable
    );

    List<Report> findByWeekStart(LocalDate weekStart);

    @Query("SELECT r FROM Report r WHERE r.weekStart >= :from AND r.weekEnd <= :to")
    List<Report> findAllInDateRange(@Param("from") LocalDate from, @Param("to") LocalDate to);

    long countByStatus(ReportStatus status);
}
