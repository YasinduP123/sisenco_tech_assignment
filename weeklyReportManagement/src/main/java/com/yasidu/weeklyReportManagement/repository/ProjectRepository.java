package com.yasidu.weeklyReportManagement.repository;

import com.yasidu.weeklyReportManagement.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {
}