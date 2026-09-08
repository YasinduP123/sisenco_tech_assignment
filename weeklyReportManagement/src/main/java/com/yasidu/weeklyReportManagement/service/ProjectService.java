package com.yasidu.weeklyReportManagement.service;


import com.yasidu.weeklyReportManagement.dto.ProjectDto;

import java.util.List;

public interface ProjectService {

    /**
     * Returns all projects/categories (visible to all authenticated users).
     */
    List<ProjectDto> getAllProjects();

    /**
     * Returns only active projects (used when populating a report's project dropdown).
     */
    List<ProjectDto> getActiveProjects();

    /**
     * Manager-only: get a single project by id.
     */
    ProjectDto getProjectById(Long id);

    /**
     * Manager-only: create a new project/category.
     */
    ProjectDto createProject(ProjectDto dto);

    /**
     * Manager-only: update an existing project's details.
     */
    ProjectDto updateProject(Long id, ProjectDto dto);

    /**
     * Manager-only: delete a project. Should fail gracefully or soft-delete
     * if reports already reference it (see implementation note below).
     */
    void deleteProject(Long id);
}