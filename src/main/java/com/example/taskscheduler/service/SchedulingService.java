package com.example.taskscheduler.service;

import com.example.taskscheduler.model.ProjectTask;
import com.example.taskscheduler.model.ScheduledTask;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class SchedulingService {

    // Schedules a list of ProjectTask objects, ensuring that dependencies are respected and detecting circular dependencies.
    public List<ScheduledTask> schedule(List<ProjectTask> tasks, LocalDate projectStartDate) {
        Objects.requireNonNull(tasks, "tasks");
        Objects.requireNonNull(projectStartDate, "projectStartDate");

        Map<Long, ProjectTask> tasksById = new LinkedHashMap<>();
        for (ProjectTask task : tasks) {
            if (tasksById.putIfAbsent(task.id(), task) != null) {
                throw new IllegalArgumentException("Duplicate task id: " + task.id());
            }
        }
        validateDependencies(tasksById);

        Map<Long, VisitState> states = new HashMap<>();
        Map<Long, ScheduledTask> scheduledById = new LinkedHashMap<>();
        for (ProjectTask task : tasks) {
            scheduleTask(task, projectStartDate, tasksById, states, scheduledById);
        }
        return new ArrayList<>(scheduledById.values());
    }

    // Schedules a single Project Task
    private ScheduledTask scheduleTask(
            ProjectTask task,
            LocalDate projectStartDate,
            Map<Long, ProjectTask> tasksById,
            Map<Long, VisitState> states,
            Map<Long, ScheduledTask> scheduledById
    ) {
        if (states.get(task.id()) == VisitState.VISITING) {
            throw new IllegalArgumentException("Circular dependency detected at task: " + task.id());
        }
        if (states.get(task.id()) == VisitState.VISITED) {
            return scheduledById.get(task.id());
        }

        states.put(task.id(), VisitState.VISITING);
        LocalDate startDate = projectStartDate;

        for (Long dependencyId : task.dependencyIds()) {
            ScheduledTask dependency = scheduleTask(
                    tasksById.get(dependencyId), projectStartDate, tasksById, states, scheduledById);
            LocalDate firstAvailableDate = dependency.endDate().plusDays(1);
            if (firstAvailableDate.isAfter(startDate)) {
                startDate = firstAvailableDate;
            }
        }

        LocalDate endDate = startDate.plusDays(task.durationDays() - 1L);
        ScheduledTask scheduledTask = new ScheduledTask(
                task.id(), task.projectId(), task.name(), task.description(), task.durationDays(),
                task.dependencyIds(), startDate, endDate);
        states.put(task.id(), VisitState.VISITED);
        scheduledById.put(task.id(), scheduledTask);
        return scheduledTask;
    }

    // Validates that all dependencies of tasks exist and that no task depends on itself
    private void validateDependencies(Map<Long, ProjectTask> tasksById) {
        for (ProjectTask task : tasksById.values()) {
            for (Long dependencyId : task.dependencyIds()) {
                if (!tasksById.containsKey(dependencyId)) {
                    throw new IllegalArgumentException(
                            "Task '%s' has unknown dependency '%s'".formatted(task.id(), dependencyId));
                }
                if (task.id().equals(dependencyId)) {
                    throw new IllegalArgumentException("Task cannot depend on itself: " + task.id());
                }
            }
        }
    }

    // VISITING means the task is in the current dependency path; finding it again indicates a circular dependency
    private enum VisitState {
        VISITING,
        VISITED
    }
}
