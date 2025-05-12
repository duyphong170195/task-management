package com.eight_seneca.task_management.applicationservice;

import com.eight_seneca.common.factory.Paging;
import com.eight_seneca.task_management.controller.request.UserCreateRequest;
import com.eight_seneca.task_management.controller.request.UserUpdateRequest;
import com.eight_seneca.task_management.controller.response.UserDetailResponse;
import com.eight_seneca.task_management.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface UserApplicationService {

    UserDetailResponse create(UserCreateRequest request);

    void update(UUID id, UserUpdateRequest request);

    UserDetailResponse get(UUID id);

    Paging<UserDetailResponse> search(String keyword, Pageable pageable);

    void delete(UUID id);
}
