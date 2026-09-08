package com.yasidu.weeklyReportManagement.service.impl;

import com.yasidu.weeklyReportManagement.dto.ProjectDto;
import com.yasidu.weeklyReportManagement.entity.Project;
import com.yasidu.weeklyReportManagement.repository.ProjectRepository;
import com.yasidu.weeklyReportManagement.service.ProjectService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;

    public List<ProjectDto> getAllProjects() {
        return projectRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProjectDto> getActiveProjects() {
        return List.of();
    }

    @Override
    public ProjectDto getProjectById(Long id) {
        return null;
    }

    public ProjectDto createProject(ProjectDto dto) {
        Project project = Project.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .active(true)
                .build();
        return toDto(projectRepository.save(project));
    }

    public ProjectDto updateProject(Long id, ProjectDto dto) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Project not found: " + id));
        project.setName(dto.getName());
        project.setDescription(dto.getDescription());
        if (dto.getActive() != null) project.setActive(dto.getActive());
        return toDto(projectRepository.save(project));
    }

    public void deleteProject(Long id) {
        if (!projectRepository.existsById(id)) {
            throw new EntityNotFoundException("Project not found: " + id);
        }
        projectRepository.deleteById(id);
    }

    private ProjectDto toDto(Project p) {
        return ProjectDto.builder()
                .id(p.getId())
                .name(p.getName())
                .description(p.getDescription())
                .active(p.getActive())
                .build();
    }
}