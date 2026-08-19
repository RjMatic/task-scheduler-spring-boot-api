package com.example.taskscheduler.model;

import java.time.LocalDate;

public record ScheduledTask(
        Long id,
        Long projectId,
        String name,
        String description,
        int durationDays,
        java.util.List<Long> dependencyIds,
        LocalDate startDate,
        LocalDate endDate
) {
}
