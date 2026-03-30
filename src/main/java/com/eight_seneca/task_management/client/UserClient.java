package com.eight_seneca.task_management.client;

import com.eight_seneca.task_management.controller.response.UserDetailResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserClient {

    private final RestTemplate restTemplate;

    public UserDetailResponse getUser(UUID userId) {
        String url = "http://localhost:8081/task/v1/user/{id}";

        return restTemplate.getForObject(
                url,
                UserDetailResponse.class,
                userId
        );
    }
}
