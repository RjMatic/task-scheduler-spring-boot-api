package com.example.taskscheduler.repository;

import com.example.taskscheduler.model.ProjectTask;
import org.springframework.stereotype.Repository;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class TaskRepository {
    // tasks are stored in application memory and are cleared whenever the application restarts
    private final Map<Long, ProjectTask> tasks = new LinkedHashMap<>();
    private long nextId = 1;

    public synchronized long nextId() { return nextId++; }

    // Find all task by projectId.
    public synchronized List<ProjectTask> findByProjectId(Long projectId) {
        return tasks.values().stream().filter(task -> task.projectId().equals(projectId)).toList();
    }

    // Find task by projectId and taskId.
    public synchronized Optional<ProjectTask> findByProjectIdAndId(Long projectId, Long id) {
        return Optional.ofNullable(tasks.get(id)).filter(task -> task.projectId().equals(projectId));
    }

    public synchronized ProjectTask save(ProjectTask task) { tasks.put(task.id(), task); return task; }

    // Delete task by id.
    public synchronized void deleteById(Long id) { tasks.remove(id); }

    // Delete all tasks by projectId.
    public synchronized void deleteByProjectId(Long projectId) {
        tasks.values().removeIf(task -> task.projectId().equals(projectId));
    }
}
