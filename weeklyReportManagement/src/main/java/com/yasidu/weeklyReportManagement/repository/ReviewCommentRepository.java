package com.yasidu.weeklyReportManagement.repository;

import com.yasidu.weeklyReportManagement.entity.ReviewComment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ReviewCommentRepository extends JpaRepository<ReviewComment, Long> {
    List<ReviewComment> findByReportIdOrderByCreatedAtDesc(Long reportId);
    Optional<ReviewComment> findFirstByReportIdOrderByCreatedAtDesc(Long reportId);
}