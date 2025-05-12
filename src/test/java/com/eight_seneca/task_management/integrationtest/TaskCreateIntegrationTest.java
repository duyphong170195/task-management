package com.eight_seneca.task_management.integrationtest;

import com.eight_seneca.common.factory.BaseResponse;
import com.eight_seneca.common.factory.BaseResponseStatus;
import com.eight_seneca.task_management.constant.MessageCode;
import com.eight_seneca.task_management.controller.request.TaskCreateRequest;
import com.eight_seneca.task_management.controller.response.TaskDetailResponse;
import com.eight_seneca.task_management.enums.TaskStatusEnum;
import com.eight_seneca.task_management.enums.TaskTypeEnum;
import com.eight_seneca.task_management.fixture.task.TaskCreateRequestFixtures;
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
class TaskCreateIntegrationTest extends AbstractIntegrationTest {


    @Autowired
    TestRestTemplate testRestTemplate;

    @Test
    void create_ValidRequest_ReturnsSuccess() {
        UUID userId = createUser();
        TaskCreateRequest taskCreateRequest = TaskCreateRequestFixtures.createTask(userId, TaskTypeEnum.BUG);
        // Arrange
        HttpEntity<TaskCreateRequest> requestEntity = new HttpEntity<>(taskCreateRequest);
        // Act
        ResponseEntity<BaseResponse<TaskDetailResponse>> response = testRestTemplate.exchange(
                "/v1/task",
                HttpMethod.POST,
                requestEntity,
                new ParameterizedTypeReference<>() {
                }
        );
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        TaskDetailResponse taskDetailResponse = response.getBody().getData();
        this.verifyTaskResponse(taskDetailResponse, taskCreateRequest);

        // TODO delete task, user
    }

    @Test
    void create_notFoundUser_returnsNotFound() {
        UUID userId = UUID.randomUUID();
        TaskCreateRequest taskCreateRequest = TaskCreateRequestFixtures.createTask(userId, TaskTypeEnum.BUG);
        // Arrange
        HttpEntity<TaskCreateRequest> requestEntity = new HttpEntity<>(taskCreateRequest);
        // Act
        ResponseEntity<BaseResponse<BaseResponseStatus>> response = testRestTemplate.exchange(
                "/v1/task",
                HttpMethod.POST,
                requestEntity,
                new ParameterizedTypeReference<>() {
                }
        );
        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(MessageCode.CREATE_TASK_USER_NOT_FOUND, response.getBody().getStatus().getCode());
    }

    private void verifyTaskResponse(TaskDetailResponse actual, TaskCreateRequest expected) {
        Assertions.assertNotNull(actual.getId());
        Assertions.assertEquals(actual.getTaskType(), expected.getTaskType());
        Assertions.assertEquals(actual.getName(), expected.getName());
        Assertions.assertEquals(actual.getStatus(), TaskStatusEnum.OPEN);
        Assertions.assertEquals(actual.getDescription(), expected.getDescription());
    }

}