package com.example.taskscheduler.model;

import java.util.List;
import java.util.Objects;

public record ProjectTask(
        Long id,
        Long projectId,
        String name,
        String description,
        int durationDays,
        List<Long> dependencyIds
) {

    public ProjectTask {
        if (id == null || id < 1) {
            throw new IllegalArgumentException("Task id must be a positive number");
        }
        if (projectId == null || projectId < 1) {
            throw new IllegalArgumentException("Project id must be a positive number");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Task name must not be blank");
        }
        if (durationDays < 1) {
            throw new IllegalArgumentException("Task duration must be at least one day");
        }
        description = description == null ? "" : description.trim();
        dependencyIds = List.copyOf(Objects.requireNonNull(dependencyIds, "dependencyIds"));
    }
}
