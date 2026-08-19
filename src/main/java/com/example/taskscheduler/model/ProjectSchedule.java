package com.example.taskscheduler.model;

import java.time.LocalDate;
import java.util.List;

public record ProjectSchedule(
        Long projectId,
        String projectName,
        LocalDate projectStartDate,
        LocalDate targetEndDate,
        LocalDate calculatedEndDate,
        boolean deadlineExceeded,
        List<ScheduledTask> tasks
) {
}
