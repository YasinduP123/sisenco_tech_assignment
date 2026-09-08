package com.yasidu.weeklyReportManagement.controller;

import com.yasidu.weeklyReportManagement.dto.ProjectDto;
import com.yasidu.weeklyReportManagement.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping
    public ResponseEntity<List<ProjectDto>> getAll() {
        return ResponseEntity.ok(projectService.getAllProjects());
    }

    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ProjectDto> create(@RequestBody ProjectDto dto) {
        return ResponseEntity.ok(projectService.createProject(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ProjectDto> update(@PathVariable Long id, @RequestBody ProjectDto dto) {
        return ResponseEntity.ok(projectService.updateProject(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }
}
