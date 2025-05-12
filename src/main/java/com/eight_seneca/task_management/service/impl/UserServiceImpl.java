package com.eight_seneca.task_management.service.impl;

import com.eight_seneca.common.exception.BadRequestException;
import com.eight_seneca.common.exception.CustomException;
import com.eight_seneca.task_management.constant.MessageCode;
import com.eight_seneca.task_management.controller.request.UserCreateRequest;
import com.eight_seneca.task_management.controller.request.UserUpdateRequest;
import com.eight_seneca.task_management.controller.response.UserDetailResponse;
import com.eight_seneca.task_management.entity.UserEntity;
import com.eight_seneca.task_management.mapper.UserMapper;
import com.eight_seneca.task_management.repository.UserRepository;
import com.eight_seneca.task_management.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    @Override
    @Transactional
    public UserEntity save(UserEntity userEntity) {
        return userRepository.save(userEntity);
    }

    @Override
    public UserEntity findByIdOrThrow(UUID id) {
        return userRepository.findById(id).orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, MessageCode.USER_NOT_FOUND));
    }

    @Override
    public UserEntity findByIdForUpdateOrThrow(UUID id) {
        return userRepository.findByIdForUpdate(id).orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, MessageCode.USER_NOT_FOUND));
    }

    @Override
    public void verifyUsernameNotExist(String username, String messageCode) {
        if(userRepository.countByUsername(username) > 0) {
            throw new CustomException(HttpStatus.BAD_REQUEST, messageCode);
        }
    }

    @Override
    public Page<UserEntity> search(String keyword, Pageable pageable) {
        return userRepository.search(keyword, pageable);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        userRepository.deleteById(id);
    }

    @Override
    public void verifyUserExist(UUID id, String messageCode) {
        if(!userRepository.existsById(id)) {
            throw new CustomException(HttpStatus.NOT_FOUND, messageCode);
        }
    }
}
