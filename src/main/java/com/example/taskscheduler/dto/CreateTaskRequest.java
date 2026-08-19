package com.example.taskscheduler.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;

public record CreateTaskRequest(
        @jakarta.validation.constraints.NotBlank String name,
        String description,
        @Positive int durationDays,
        @NotNull List<@Positive Long> dependencyIds
) {
}
