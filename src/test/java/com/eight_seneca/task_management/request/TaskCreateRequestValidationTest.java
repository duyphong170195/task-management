package com.eight_seneca.task_management.request;

import com.eight_seneca.task_management.controller.request.TaskCreateRequest;
import com.eight_seneca.task_management.enums.TaskTypeEnum;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TaskCreateRequestValidationTest {

    private static Validator validator;
    private TaskCreateRequest taskCreateRequest;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @BeforeEach
    void setUp() {
        taskCreateRequest = TaskCreateRequest.builder()
            .name("Valid Task Name")
            .description("Sample description")
            .taskType(TaskTypeEnum.FEATURE)
            .userId(UUID.randomUUID())
            .severity("MEDIUM")
            .stepsToReproduce("1. Do this\n2. Do that")
            .businessValue(50)
            .deadline(Instant.now().plusSeconds(86400))
            .build();
    }

    @Test
    void whenAllFieldsValid_thenNoConstraintViolations() {
        // Act
        Set<ConstraintViolation<TaskCreateRequest>> violations = validator.validate(taskCreateRequest);

        // Assert
        assertTrue(violations.isEmpty(), "No violations should occur with valid data");
    }

    @Test
    void whenNameIsNull_thenValidationFails() {
        // Arrange
        taskCreateRequest.setName(null);

        // Act
        Set<ConstraintViolation<TaskCreateRequest>> violations = validator.validate(taskCreateRequest);

        // Assert
        assertEquals(1, violations.size(), "Should have one violation for null name");
        assertEquals("must not be blank", violations.iterator().next().getMessage());
        assertEquals("name", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void whenNameIsBlank_thenValidationFails() {
        // Arrange
        taskCreateRequest.setName("   ");

        // Act
        Set<ConstraintViolation<TaskCreateRequest>> violations = validator.validate(taskCreateRequest);

        // Assert
        assertEquals(1, violations.size(), "Should have one violation for blank name");
        assertEquals("must not be blank", violations.iterator().next().getMessage());
        assertEquals("name", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void whenTaskTypeIsNull_thenValidationFails() {
        // Arrange
        taskCreateRequest.setTaskType(null);

        // Act
        Set<ConstraintViolation<TaskCreateRequest>> violations = validator.validate(taskCreateRequest);

        // Assert
        assertEquals(1, violations.size(), "Should have one violation for null taskType");
        assertEquals("must not be null", violations.iterator().next().getMessage());
        assertEquals("taskType", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void whenUserIdIsNull_thenValidationFails() {
        // Arrange
        taskCreateRequest.setUserId(null);

        // Act
        Set<ConstraintViolation<TaskCreateRequest>> violations = validator.validate(taskCreateRequest);

        // Assert
        assertEquals(1, violations.size(), "Should have one violation for null userId");
        assertEquals("must not be null", violations.iterator().next().getMessage());
        assertEquals("userId", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void whenMultipleFieldsInvalid_thenMultipleViolations() {
        // Arrange
        taskCreateRequest.setName("");
        taskCreateRequest.setTaskType(null);
        taskCreateRequest.setUserId(null);

        // Act
        Set<ConstraintViolation<TaskCreateRequest>> violations = validator.validate(taskCreateRequest);

        // Assert
        assertEquals(3, violations.size(), "Should have three violations for invalid fields");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("name")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("taskType")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("userId")));
    }
}
