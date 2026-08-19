package com.example.taskscheduler.controller;

import com.example.taskscheduler.dto.CreateProjectRequest;
import com.example.taskscheduler.dto.UpdateProjectRequest;
import com.example.taskscheduler.model.Project;
import com.example.taskscheduler.model.ProjectSchedule;
import com.example.taskscheduler.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/projects")
@Tag(name = "Projects", description = "Manage project plans and view calculated schedules")
public class ProjectController {
    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) { this.projectService = projectService; }

    @GetMapping
    @Operation(summary = "View all projects")
    public List<Project> findAll() { return projectService.findAll(); }

    @GetMapping("/{projectId}")
    @Operation(summary = "View a project by ID")
    public Project findById(@PathVariable Long projectId) { return projectService.findById(projectId); }

    @PostMapping
    @Operation(summary = "Create a project")
    public ResponseEntity<Project> create(@Valid @RequestBody CreateProjectRequest request) {
        Project created = projectService.create(request);
        return ResponseEntity.created(URI.create("/api/projects/" + created.id())).body(created);
    }

    @PutMapping("/{projectId}")
    @Operation(summary = "Update a project")
    public Project update(@PathVariable Long projectId, @Valid @RequestBody UpdateProjectRequest request) {
        return projectService.update(projectId, request);
    }

    @DeleteMapping("/{projectId}")
    @Operation(summary = "Delete a project and all its tasks")
    public ResponseEntity<Void> delete(@PathVariable Long projectId) {
        projectService.delete(projectId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{projectId}/schedule")
    @Operation(summary = "View the automatically calculated project schedule")
    public ProjectSchedule schedule(@PathVariable Long projectId) {
        return projectService.getSchedule(projectId);
    }
}
