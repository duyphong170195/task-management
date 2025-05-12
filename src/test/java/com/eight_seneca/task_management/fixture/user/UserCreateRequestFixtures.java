package com.eight_seneca.task_management.fixture.user;

import com.eight_seneca.task_management.controller.request.UserCreateRequest;
import com.eight_seneca.task_management.enums.TaskTypeEnum;

import java.time.Instant;
import java.util.UUID;

public class UserCreateRequestFixtures {

    public static UserCreateRequest createUserRequest() {
        UserCreateRequest taskRequest = UserCreateRequest.builder()
                .fullName("Tom cruise")
                .username("tomcruise")
                .build();
        return taskRequest;
    }
}
