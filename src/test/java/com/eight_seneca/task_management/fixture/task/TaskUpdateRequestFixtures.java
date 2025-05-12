package com.eight_seneca.task_management.fixture.task;

import com.eight_seneca.task_management.controller.request.TaskCreateRequest;
import com.eight_seneca.task_management.controller.request.TaskUpdateRequest;
import com.eight_seneca.task_management.enums.TaskTypeEnum;

import java.time.Instant;
import java.util.UUID;

public class TaskUpdateRequestFixtures {

    public static TaskUpdateRequest createBugRequest(UUID userId) {
        TaskUpdateRequest taskRequest = TaskUpdateRequest.builder()
                .name("Fix Payment Processing Bug")
                .description("Resolve issue where payment fails for certain credit cards.")
                .userId(userId)
                .severity("CRITICAL")
                .stepsToReproduce("1. Go to checkout\n2. Enter credit card details\n3. Submit payment")
                .build();
        return taskRequest;
    }

    public static TaskUpdateRequest createFeatureRequest() {
        TaskUpdateRequest taskRequest = TaskUpdateRequest.builder()
                .name("Implement Login Feature")
                .description("Develop a secure user login system with email and password.")
                .userId(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
                .businessValue(100)
                .deadline(Instant.parse("2025-06-01T12:00:00Z"))
                .build();
        return taskRequest;
    }
}
