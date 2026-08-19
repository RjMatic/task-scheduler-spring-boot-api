package com.example.taskscheduler.service;

import com.example.taskscheduler.dto.CreateTaskRequest;
import com.example.taskscheduler.dto.UpdateTaskRequest;
import com.example.taskscheduler.exception.TaskNotFoundException;
import com.example.taskscheduler.model.ProjectTask;
import com.example.taskscheduler.repository.TaskRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class TaskService {
    private final TaskRepository repository;
    private final ProjectService projectService;
    private final SchedulingService schedulingService;

    public TaskService(TaskRepository repository, ProjectService projectService,
                       SchedulingService schedulingService) {
        this.repository = repository;
        this.projectService = projectService;
        this.schedulingService = schedulingService;
    }

    public List<ProjectTask> findAll(Long projectId) {
        projectService.findById(projectId);
        return repository.findByProjectId(projectId);
    }

    // Find a task by its ID within a specific project
    public ProjectTask findById(Long projectId, Long taskId) {
        projectService.findById(projectId);
        return repository.findByProjectIdAndId(projectId, taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));
    }

    // Create a new task within a specific project
    public ProjectTask create(Long projectId, CreateTaskRequest request) {

        projectService.findById(projectId);
        validateDependencies(projectId, null, request.dependencyIds());

        validateUniqueTaskName(projectId, request.name().trim());

        ProjectTask task = new ProjectTask(repository.nextId(), projectId, request.name().trim(),
                request.description(), request.durationDays(), normalize(request.dependencyIds()));
        List<ProjectTask> proposedTasks = new ArrayList<>(repository.findByProjectId(projectId));
        proposedTasks.add(task);
        validatePlan(proposedTasks);
        return repository.save(task);
    }

    // Update an existing task within a specific project
    public ProjectTask update(Long projectId, Long taskId, UpdateTaskRequest request) {
        findById(projectId, taskId);
        validateDependencies(projectId, taskId, request.dependencyIds());
        ProjectTask updated = new ProjectTask(taskId, projectId, request.name().trim(),
                request.description(), request.durationDays(), normalize(request.dependencyIds()));
        List<ProjectTask> proposedTasks = repository.findByProjectId(projectId).stream()
                .map(task -> task.id().equals(taskId) ? updated : task).toList();
        validatePlan(proposedTasks);
        return repository.save(updated);
    }

    // Delete a task within a specific project
    public void delete(Long projectId, Long taskId) {
        findById(projectId, taskId);
        List<Long> dependents = repository.findByProjectId(projectId).stream()
                .filter(task -> task.dependencyIds().contains(taskId)).map(ProjectTask::id).toList();
        if (!dependents.isEmpty()) {
            throw new IllegalArgumentException("Task '%s' cannot be deleted because it is required by: %s"
                    .formatted(taskId, dependents));
        }
        repository.deleteById(taskId);
    }

    // validate that all dependencies exist and that a task does not depend on itself
    private void validateDependencies(Long projectId, Long taskId, List<Long> dependencyIds) {
        List<Long> normalized = normalize(dependencyIds);
        if (taskId != null && normalized.contains(taskId)) {
            throw new IllegalArgumentException("Task cannot depend on itself: " + taskId);
        }
        for (Long dependencyId : normalized) {
            if (repository.findByProjectIdAndId(projectId, dependencyId).isEmpty()) {
                throw new IllegalArgumentException(
                        "Unknown dependency in project %s: %s".formatted(projectId, dependencyId));
            }
        }
    }

    // Remove duplicate dependency IDs from the list
    private List<Long> normalize(List<Long> dependencyIds) {
        return dependencyIds.stream().distinct().toList();
    }

    // Check that the task name is unique within the project
    private void validateUniqueTaskName(Long projectId, String taskName) {
        List<ProjectTask> tasks = repository.findByProjectId(projectId);
        for (ProjectTask task : tasks) {
            if (task.name().equalsIgnoreCase(taskName)) {
                throw new IllegalArgumentException("Task name '%s' is already used in project %s"
                        .formatted(taskName, projectId));
            }
        }
    }

    // Validate that the proposed task plan can be scheduled without circular dependencies
    private void validatePlan(List<ProjectTask> tasks) {
        schedulingService.schedule(tasks, LocalDate.of(2000, 1, 1));
    }
}
