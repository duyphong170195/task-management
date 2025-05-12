package com.eight_seneca.task_management.service.impl;

import com.eight_seneca.common.exception.CustomException;
import com.eight_seneca.task_management.constant.MessageCode;
import com.eight_seneca.task_management.controller.response.TaskDetailResponse;
import com.eight_seneca.task_management.entity.TaskEntity;
import com.eight_seneca.task_management.enums.TaskStatusEnum;
import com.eight_seneca.task_management.enums.TaskTypeEnum;
import com.eight_seneca.task_management.repository.TaskRepository;
import com.eight_seneca.task_management.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;

    @Override
    public TaskEntity save(TaskEntity taskEntity) {
        return taskRepository.save(taskEntity);
    }

    @Override
    public TaskEntity findByIdForUpdateOrThrow(UUID id) {
        return taskRepository.findByIdForUpdate(id).orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, MessageCode.TASK_NOT_FOUND));
    }

    @Override
    public Page<TaskDetailResponse> search(String keyword,
                                           TaskStatusEnum status,
                                           UUID userId,
                                           TaskTypeEnum taskType, Pageable pageable) {
        return taskRepository.search(keyword, status, userId, taskType, pageable);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        taskRepository.deleteById(id);
    }

    @Override
    public void verifyTaskExist(UUID id) {

    }

    @Override
    public TaskDetailResponse findTaskDetailResponseByIdOrThrow(UUID id) {
        return taskRepository.findTaskDetailResponseById(id).orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, MessageCode.TASK_NOT_FOUND));
    }
}
