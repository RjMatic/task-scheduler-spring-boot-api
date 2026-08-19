package com.example.taskscheduler.service;

import com.example.taskscheduler.dto.CreateProjectRequest;
import com.example.taskscheduler.dto.UpdateProjectRequest;
import com.example.taskscheduler.exception.ProjectNotFoundException;
import com.example.taskscheduler.model.Project;
import com.example.taskscheduler.model.ProjectSchedule;
import com.example.taskscheduler.model.ScheduledTask;
import com.example.taskscheduler.repository.ProjectRepository;
import com.example.taskscheduler.repository.TaskRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;

@Service
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final SchedulingService schedulingService;

    public ProjectService(ProjectRepository projectRepository, TaskRepository taskRepository,
                          SchedulingService schedulingService) {
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.schedulingService = schedulingService;
    }

    public List<Project> findAll() { return projectRepository.findAll(); }

    // Find a project by its ID, throwing an exception if not found
    public Project findById(Long id) {
        return projectRepository.findById(id).orElseThrow(() -> new ProjectNotFoundException(id));
    }

    // Create a new project
    public Project create(CreateProjectRequest request) {
        validateDates(request.startDate(), request.targetEndDate());
        String name = request.name().trim();
        validateUniqueName(name, null);
        return projectRepository.save(new Project(projectRepository.nextId(), name,
                normalizeDescription(request.description()), request.startDate(), request.targetEndDate()));
    }

    // Update an existing project by its ID
    public Project update(Long id, UpdateProjectRequest request) {
        findById(id);
        validateDates(request.startDate(), request.targetEndDate());
        String name = request.name().trim();
        validateUniqueName(name, id);
        return projectRepository.save(new Project(id, name,
                normalizeDescription(request.description()), request.startDate(), request.targetEndDate()));
    }

    // Delete a project by its ID, along with all associated tasks
    public void delete(Long id) {
        findById(id);
        taskRepository.deleteByProjectId(id);
        projectRepository.deleteById(id);
    }

    // Get the schedule for a project by its ID
    public ProjectSchedule getSchedule(Long projectId) {
        Project project = findById(projectId);
        List<ScheduledTask> tasks = schedulingService.schedule(
                taskRepository.findByProjectId(projectId), project.startDate());

        LocalDate calculatedEndDate = tasks.stream().map(ScheduledTask::endDate)
                .max(LocalDate::compareTo).orElse(project.startDate());

        boolean deadlineExceeded = project.targetEndDate() != null
                && calculatedEndDate.isAfter(project.targetEndDate());

        return new ProjectSchedule(project.id(), project.name(), project.startDate(),
                project.targetEndDate(), calculatedEndDate, deadlineExceeded, tasks);
    }

    // Validate that the target end date
    private void validateDates(LocalDate startDate, LocalDate targetEndDate) {
        if (targetEndDate != null && targetEndDate.isBefore(startDate)) {
            throw new IllegalArgumentException("Target end date cannot be before project start date");
        }
    }

    private String normalizeDescription(String description) {
        return description == null ? "" : description.trim();
    }

    // Validate that the project name is unique
    private void validateUniqueName(String name, Long excludedProjectId) {
        if (projectRepository.existsByNameIgnoringCase(name, excludedProjectId)) {
            throw new IllegalArgumentException("Project name already exists: " + name);
        }
    }
}
