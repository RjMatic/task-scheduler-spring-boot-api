package com.example.taskscheduler.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ProjectWorkflowApiTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void supportsCompleteProjectTaskAndScheduleWorkflow() throws Exception {
        mockMvc.perform(post("/api/projects").contentType(MediaType.APPLICATION_JSON).content("""
                {"name":"Website launch","description":"Launch project",
                 "startDate":"2026-08-19","targetEndDate":"2026-08-25"}
                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));

        mockMvc.perform(post("/api/projects/1/tasks").contentType(MediaType.APPLICATION_JSON).content("""
                {"name":"Design","description":"Create designs","durationDays":3,"dependencyIds":[]}
                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.projectId").value(1));

        mockMvc.perform(post("/api/projects/1/tasks").contentType(MediaType.APPLICATION_JSON).content("""
                {"name":"Build","description":"Build site","durationDays":5,"dependencyIds":[1]}
                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2));

        mockMvc.perform(get("/api/projects/1/schedule"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.calculatedEndDate").value("2026-08-26"))
                .andExpect(jsonPath("$.deadlineExceeded").value(true))
                .andExpect(jsonPath("$.tasks[1].startDate").value("2026-08-22"));

        mockMvc.perform(put("/api/projects/1/tasks/2").contentType(MediaType.APPLICATION_JSON).content("""
                {"name":"Build website","description":"Updated","durationDays":2,"dependencyIds":[1]}
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.durationDays").value(2));

        mockMvc.perform(delete("/api/projects/1/tasks/1"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/projects/{projectId}/schedule']").exists());

        mockMvc.perform(delete("/api/projects/1"))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/projects/1/tasks"))
                .andExpect(status().isNotFound());
    }

    @Test
    void rejectsDependenciesFromAnotherProject() throws Exception {
        mockMvc.perform(post("/api/projects").contentType(MediaType.APPLICATION_JSON).content("""
                {"name":"First project","description":"","startDate":"2026-08-19","targetEndDate":null}
                """))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/projects/1/tasks").contentType(MediaType.APPLICATION_JSON).content("""
                {"name":"First task","description":"","durationDays":1,"dependencyIds":[]}
                """))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/projects").contentType(MediaType.APPLICATION_JSON).content("""
                {"name":"Second project","description":"","startDate":"2026-08-19","targetEndDate":null}
                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/projects/2/tasks").contentType(MediaType.APPLICATION_JSON).content("""
                {"name":"Invalid","description":"","durationDays":1,"dependencyIds":[1]}
                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsDuplicateProjectNamesIgnoringCaseAndSpaces() throws Exception {
        mockMvc.perform(post("/api/projects").contentType(MediaType.APPLICATION_JSON).content("""
                {"name":"Website Launch","description":"","startDate":"2026-08-19","targetEndDate":null}
                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/projects").contentType(MediaType.APPLICATION_JSON).content("""
                {"name":"  website launch  ","description":"Duplicate","startDate":"2026-08-20","targetEndDate":null}
                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Project name already exists: website launch"));
    }
}
