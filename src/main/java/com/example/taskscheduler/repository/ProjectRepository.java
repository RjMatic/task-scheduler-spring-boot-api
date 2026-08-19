package com.example.taskscheduler.repository;

import com.example.taskscheduler.model.Project;
import org.springframework.stereotype.Repository;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class ProjectRepository {
    // projects are stored in application memory and are cleared whenever the application restarts
    private final Map<Long, Project> projects = new LinkedHashMap<>();
    private long nextId = 1;

    public synchronized long nextId() { return nextId++; }

    // Find all projects in the repository
    public synchronized List<Project> findAll() { return new ArrayList<>(projects.values()); }

    // Find a project by its ID
    public synchronized Optional<Project> findById(Long id) { return Optional.ofNullable(projects.get(id)); }

    // Check if a project exists by its name
    public synchronized boolean existsByNameIgnoringCase(String name, Long excludedId) {
        return projects.values().stream()
                .filter(project -> excludedId == null || !project.id().equals(excludedId))
                .anyMatch(project -> project.name().equalsIgnoreCase(name));
    }
    public synchronized Project save(Project project) { projects.put(project.id(), project); return project; }

    // Delete a project by its ID
    public synchronized void deleteById(Long id) { projects.remove(id); }
}
