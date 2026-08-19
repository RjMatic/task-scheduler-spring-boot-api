package com.example.taskscheduler.controller;

import com.example.taskscheduler.dto.CreateTaskRequest;
import com.example.taskscheduler.dto.UpdateTaskRequest;
import com.example.taskscheduler.model.ProjectTask;
import com.example.taskscheduler.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/tasks")
@Tag(name = "Project Tasks", description = "Manage tasks belonging to a project")
public class TaskController {
    private final TaskService taskService;

    public TaskController(TaskService taskService) { this.taskService = taskService; }

    @GetMapping
    @Operation(summary = "View all tasks in a project")
    public List<ProjectTask> findAll(@PathVariable Long projectId) {
        return taskService.findAll(projectId);
    }

    @GetMapping("/{taskId}")
    @Operation(summary = "View a task in a project")
    public ProjectTask findById(@PathVariable Long projectId, @PathVariable Long taskId) {
        return taskService.findById(projectId, taskId);
    }

    // this endpoint creates a task under the project identified in the URL
    @PostMapping
    @Operation(summary = "Add a task to a project; dates are calculated by the schedule endpoint")
    public ResponseEntity<ProjectTask> create(@PathVariable Long projectId,
                                               @Valid @RequestBody CreateTaskRequest request) {
        ProjectTask created = taskService.create(projectId, request);
        return ResponseEntity.created(URI.create(
                "/api/projects/%s/tasks/%s".formatted(projectId, created.id()))).body(created);
    }

    @PutMapping("/{taskId}")
    @Operation(summary = "Update a task; the project schedule is automatically recalculated")
    public ProjectTask update(@PathVariable Long projectId, @PathVariable Long taskId,
                              @Valid @RequestBody UpdateTaskRequest request) {
        return taskService.update(projectId, taskId, request);
    }

    @DeleteMapping("/{taskId}")
    @Operation(summary = "Delete a task if no other task depends on it")
    public ResponseEntity<Void> delete(@PathVariable Long projectId, @PathVariable Long taskId) {
        taskService.delete(projectId, taskId);
        return ResponseEntity.noContent().build();
    }
}
