package com.eight_seneca.task_management.fixture.task;

import com.eight_seneca.task_management.controller.request.TaskCreateRequest;
import com.eight_seneca.task_management.enums.TaskTypeEnum;

import java.time.Instant;
import java.util.UUID;

public class TaskCreateRequestFixtures {

    public static TaskCreateRequest createTask(UUID userId, TaskTypeEnum taskType) {
        TaskCreateRequest taskRequest = TaskCreateRequest.builder()
                .name("Implement Login Feature")
                .description("Develop a secure user login system with email and password.")
                .taskType(taskType)
                .userId(userId)
                .severity("HIGH")
                .stepsToReproduce("1. Navigate to login page\n2. Enter credentials\n3. Submit form")
                .build();
        return taskRequest;
    }

    public static TaskCreateRequest createBugRequest() {
        return createTask(UUID.randomUUID(), TaskTypeEnum.BUG);
    }

    public static TaskCreateRequest createFeatureRequest() {
        TaskCreateRequest taskRequest = TaskCreateRequest.builder()
                .name("Implement Login Feature")
                .description("Develop a secure user login system with email and password.")
                .taskType(TaskTypeEnum.FEATURE)
                .userId(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
                .businessValue(100)
                .deadline(Instant.parse("2025-06-01T12:00:00Z"))
                .build();
        return taskRequest;
    }
}
