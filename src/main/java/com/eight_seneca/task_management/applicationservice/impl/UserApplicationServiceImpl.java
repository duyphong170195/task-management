package com.eight_seneca.task_management.applicationservice.impl;

import com.eight_seneca.common.factory.Paging;
import com.eight_seneca.common.util.SqlUtil;
import com.eight_seneca.task_management.applicationservice.UserApplicationService;
import com.eight_seneca.task_management.constant.MessageCode;
import com.eight_seneca.task_management.controller.request.UserCreateRequest;
import com.eight_seneca.task_management.controller.request.UserUpdateRequest;
import com.eight_seneca.task_management.controller.response.UserDetailResponse;
import com.eight_seneca.task_management.entity.UserEntity;
import com.eight_seneca.task_management.mapper.UserMapper;
import com.eight_seneca.task_management.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserApplicationServiceImpl implements UserApplicationService {


    private final UserService userService;
    @Override
    @Transactional
    public UserDetailResponse create(UserCreateRequest request) {
        userService.verifyUsernameNotExist(request.getUsername(), MessageCode.CREATE_USER_USERNAME_EXISTED);

        UserEntity userEntity = userService.save(UserMapper.INSTANCE.toEntity(request));
        return UserMapper.INSTANCE.toDetail(userEntity);
    }

    @Override
    @Transactional
    public void update(UUID id, UserUpdateRequest request) {

        UserEntity userEntity = userService.findByIdForUpdateOrThrow(id);
        UserMapper.INSTANCE.updateEntity(request, userEntity);
        userService.save(userEntity);
    }

    @Override
    public UserDetailResponse get(UUID id) {
        return UserMapper.INSTANCE.toDetail(userService.findByIdOrThrow(id));
    }

    @Override
    public Paging<UserDetailResponse> search(String keyword, Pageable pageable) {

        if (!StringUtils.hasLength(keyword)) {
            keyword = null;
        } else {
            keyword = SqlUtil.encodeKeyword(keyword.trim().toLowerCase());
        }

        Page<UserDetailResponse> userDetailResponses = userService.search(keyword, pageable)
                .map(UserMapper.INSTANCE::toDetail);

        return Paging.<UserDetailResponse>builder()
                .content(userDetailResponses.getContent())
                .totalElements(userDetailResponses.getTotalElements())
                .pageNumber(userDetailResponses.getNumber())
                .pageSize(userDetailResponses.getSize())
                .build();
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        userService.verifyUserExist(id, MessageCode.USER_NOT_FOUND);
        userService.delete(id);
    }
}
