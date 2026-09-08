package com.yasidu.weeklyReportManagement.repository;

import com.yasidu.weeklyReportManagement.entity.TaskEntry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskEntryRepository extends JpaRepository<TaskEntry, Long> {
}