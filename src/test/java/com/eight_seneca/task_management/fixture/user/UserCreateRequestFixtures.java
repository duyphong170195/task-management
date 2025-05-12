package com.eight_seneca.task_management.fixture.user;

import com.eight_seneca.task_management.controller.request.UserCreateRequest;
import com.eight_seneca.task_management.util.UsernameGenerator;


public class UserCreateRequestFixtures {

    public static UserCreateRequest createUserRequest() {
        UserCreateRequest taskRequest = UserCreateRequest.builder()
                .fullName(UsernameGenerator.generateRandomUsername(10))
                .username(UsernameGenerator.generateRandomUsername(10))
                .build();
        return taskRequest;
    }
}
