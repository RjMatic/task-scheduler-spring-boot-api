package com.example.taskscheduler.model;

import java.time.LocalDate;

public record Project(
        Long id,
        String name,
        String description,
        LocalDate startDate,
        LocalDate targetEndDate
) {
}
