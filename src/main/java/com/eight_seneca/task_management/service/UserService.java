package com.eight_seneca.task_management.service;

import com.eight_seneca.task_management.constant.MessageCode;
import com.eight_seneca.task_management.controller.request.UserCreateRequest;
import com.eight_seneca.task_management.controller.request.UserUpdateRequest;
import com.eight_seneca.task_management.controller.response.UserDetailResponse;
import com.eight_seneca.task_management.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface UserService {

    UserEntity save(UserEntity userEntity);

    UserEntity findByIdOrThrow(UUID id);

    Page<UserEntity> search(String keyword, Pageable pageable);

    void delete(UUID id);

    void verifyUserExist(UUID id, String messageCode);

    UserEntity findByIdForUpdateOrThrow(UUID id);

    void verifyUsernameNotExist(String username, String messageCode);
}
