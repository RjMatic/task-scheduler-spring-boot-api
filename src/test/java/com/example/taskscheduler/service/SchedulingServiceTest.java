package com.example.taskscheduler.service;

import com.example.taskscheduler.model.ProjectTask;
import com.example.taskscheduler.model.ScheduledTask;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SchedulingServiceTest {

    private final SchedulingService service = new SchedulingService();
    private final LocalDate projectStart = LocalDate.of(2026, 8, 19);

    @Test
    void schedulesTasksAfterTheirLatestDependency() {
        List<ProjectTask> tasks = List.of(
                new ProjectTask(1L, 1L, "A", "", 3, List.of()),
                new ProjectTask(2L, 1L, "B", "", 2, List.of(1L)),
                new ProjectTask(3L, 1L, "C", "", 5, List.of(1L)),
                new ProjectTask(4L, 1L, "D", "", 1, List.of(2L, 3L))
        );

        List<ScheduledTask> result = service.schedule(tasks, projectStart);

        assertThat(result).extracting(ScheduledTask::id).containsExactly(1L, 2L, 3L, 4L);
        assertThat(result.get(0).startDate()).isEqualTo(projectStart);
        assertThat(result.get(0).endDate()).isEqualTo(LocalDate.of(2026, 8, 21));
        assertThat(result.get(3).startDate()).isEqualTo(LocalDate.of(2026, 8, 27));
        assertThat(result.get(3).endDate()).isEqualTo(LocalDate.of(2026, 8, 27));
    }

    @Test
    void rejectsCircularDependencies() {
        List<ProjectTask> tasks = List.of(
                new ProjectTask(1L, 1L, "A", "", 1, List.of(2L)),
                new ProjectTask(2L, 1L, "B", "", 1, List.of(1L))
        );

        assertThatThrownBy(() -> service.schedule(tasks, projectStart))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Circular dependency");
    }

    @Test
    void rejectsUnknownDependencies() {
        List<ProjectTask> tasks = List.of(
                new ProjectTask(1L, 1L, "A", "", 1, List.of(99L))
        );

        assertThatThrownBy(() -> service.schedule(tasks, projectStart))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("unknown dependency");
    }
}
