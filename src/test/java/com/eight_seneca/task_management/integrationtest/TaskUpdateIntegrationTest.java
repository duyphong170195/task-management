package com.eight_seneca.task_management.integrationtest;

import com.eight_seneca.common.factory.BaseResponse;
import com.eight_seneca.task_management.constant.MessageCode;
import com.eight_seneca.task_management.controller.request.TaskUpdateRequest;
import com.eight_seneca.task_management.integrationtest.response.TaskCreateResponse;
import com.eight_seneca.task_management.controller.response.TaskDetailResponse;
import com.eight_seneca.task_management.enums.TaskStatusEnum;
import com.eight_seneca.task_management.fixture.task.TaskUpdateRequestFixtures;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TaskUpdateIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    TestRestTemplate testRestTemplate;

    @Test
    void update_ValidRequest_ReturnsSuccess() {
        // create user and task
        TaskCreateResponse taskCreateResponse = createTask();
        TaskUpdateRequest task = TaskUpdateRequestFixtures.createBugRequest(taskCreateResponse.getUserId());
        // Arrange
        HttpEntity<TaskUpdateRequest> requestEntity = new HttpEntity<>(task);
        // Act
        ResponseEntity<BaseResponse<Void>> response = testRestTemplate.exchange(
                "/v1/task/" + taskCreateResponse.getId(),
                HttpMethod.PUT,
                requestEntity,
                new ParameterizedTypeReference<>() {
                }
        );
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());

        // get detail to verify
        TaskDetailResponse taskDetailResponse = getTask(taskCreateResponse.getId());
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());

        this.verifyTaskResponse(taskDetailResponse, task);
        // TODO delete task, user
    }

    @Test
    void update_notFoundUser_returnsNotFound() {
        // create user and task
        TaskCreateResponse taskCreateResponse = createTask();
        TaskUpdateRequest task = TaskUpdateRequestFixtures.createBugRequest(taskCreateResponse.getUserId());
        // Arrange
        HttpEntity<TaskUpdateRequest> requestEntity = new HttpEntity<>(task);
        // Act
        ResponseEntity<BaseResponse<Void>> response = testRestTemplate.exchange(
                "/v1/task/" + UUID.randomUUID(),
                HttpMethod.PUT,
                requestEntity,
                new ParameterizedTypeReference<>() {
                }
        );
        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(MessageCode.TASK_NOT_FOUND, response.getBody().getStatus().getCode());
        // TODO delete task, user
    }

    @Test
    void update_UserNotFound_ReturnNotFound() {
        // create user and task
        TaskCreateResponse taskCreateResponse = createTask();
        TaskUpdateRequest task = TaskUpdateRequestFixtures.createBugRequest(UUID.randomUUID());
        // Arrange
        HttpEntity<TaskUpdateRequest> requestEntity = new HttpEntity<>(task);
        // Act
        ResponseEntity<BaseResponse<Void>> response = testRestTemplate.exchange(
                "/v1/task/" + taskCreateResponse.getId(),
                HttpMethod.PUT,
                requestEntity,
                new ParameterizedTypeReference<>() {
                }
        );
        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(MessageCode.UPDATE_TASK_USER_NOT_FOUND, response.getBody().getStatus().getCode());
        // TODO delete task, user
    }

    private void verifyTaskResponse(TaskDetailResponse actual, TaskUpdateRequest expected) {
        Assertions.assertEquals(actual.getName(), expected.getName());
        Assertions.assertEquals(actual.getStatus(), TaskStatusEnum.OPEN);
        Assertions.assertEquals(actual.getDescription(), expected.getDescription());
    }

}