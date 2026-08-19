package com.example.taskscheduler.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record CreateProjectRequest(
        @NotBlank String name,
        String description,
        @NotNull LocalDate startDate,
        LocalDate targetEndDate
) {
}
