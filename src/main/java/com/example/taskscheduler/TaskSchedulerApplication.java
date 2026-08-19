package com.example.taskscheduler;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class TaskSchedulerApplication {
    public static void main(String[] args) {
        SpringApplication.run(TaskSchedulerApplication.class, args);
    }

    @Bean
    OpenAPI taskSchedulerOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Project Task Scheduler API")
                .version("1.0")
                .description("Manage in-memory project tasks and calculate their schedule."));
    }
}
