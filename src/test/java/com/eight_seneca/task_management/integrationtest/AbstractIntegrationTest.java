package com.eight_seneca.task_management.integrationtest;

import com.eight_seneca.common.factory.BaseResponse;
import com.eight_seneca.task_management.controller.request.TaskCreateRequest;
import com.eight_seneca.task_management.controller.request.UserCreateRequest;
import com.eight_seneca.task_management.integrationtest.response.TaskCreateResponse;
import com.eight_seneca.task_management.controller.response.TaskDetailResponse;
import com.eight_seneca.task_management.controller.response.UserDetailResponse;
import com.eight_seneca.task_management.enums.TaskTypeEnum;
import com.eight_seneca.task_management.fixture.task.TaskCreateRequestFixtures;
import com.eight_seneca.task_management.fixture.user.UserCreateRequestFixtures;
import com.sun.net.httpserver.Headers;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
public abstract class AbstractIntegrationTest {

    @Autowired
    TestRestTemplate testRestTemplate;

    protected UUID createUser() {
        UserCreateRequest request = UserCreateRequestFixtures.createUserRequest();

        // Arrange
        HttpEntity<UserCreateRequest> requestEntity = new HttpEntity<>(request);

        // Act
        ResponseEntity<BaseResponse<UserDetailResponse>> response = testRestTemplate.exchange(
                "/v1/user",
                HttpMethod.POST,
                requestEntity,
                new ParameterizedTypeReference<>() {
                });

        assertEquals(HttpStatus.OK, response.getStatusCode());

       return response.getBody().getData().getId();
    }

    protected TaskCreateResponse createTask() {
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
        return new TaskCreateResponse(response.getBody().getData().getId(), userId);
    }

    protected TaskDetailResponse getTask(UUID id) {

        HttpEntity http = new HttpEntity(new Headers());
        ResponseEntity<BaseResponse<TaskDetailResponse>> response = testRestTemplate.exchange(
                "/v1/task/" + id,
                HttpMethod.GET,
                http,
                new ParameterizedTypeReference<>() {
                }
        );
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());

        return response.getBody().getData();
    }
}
