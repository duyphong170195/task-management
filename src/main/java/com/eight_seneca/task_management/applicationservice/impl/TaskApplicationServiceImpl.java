package com.eight_seneca.task_management.applicationservice.impl;

import com.eight_seneca.common.factory.Paging;
import com.eight_seneca.common.util.SqlUtil;
import com.eight_seneca.task_management.applicationservice.TaskApplicationService;
import com.eight_seneca.task_management.constant.MessageCode;
import com.eight_seneca.task_management.controller.request.TaskCreateRequest;
import com.eight_seneca.task_management.controller.request.TaskUpdateRequest;
import com.eight_seneca.task_management.controller.response.TaskDetailResponse;
import com.eight_seneca.task_management.entity.TaskEntity;
import com.eight_seneca.task_management.enums.TaskStatusEnum;
import com.eight_seneca.task_management.enums.TaskTypeEnum;
import com.eight_seneca.task_management.mapper.TaskMapper;
import com.eight_seneca.task_management.service.TaskService;
import com.eight_seneca.task_management.service.TaskTypeHandler;
import com.eight_seneca.task_management.service.UserService;
import com.eight_seneca.task_management.service.factory.TaskTypeFactory;
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
public class TaskApplicationServiceImpl implements TaskApplicationService {

    private final TaskService taskService;

    private final UserService userService;

    private final TaskTypeFactory taskTypeFactory;

    @Override
    @Transactional
    public TaskDetailResponse create(TaskCreateRequest request) {
        userService.verifyUserExist(request.getUserId(), MessageCode.CREATE_TASK_USER_NOT_FOUND);

        TaskEntity taskEntity = taskService.save(TaskMapper.INSTANCE.toEntity(request));

        TaskTypeHandler taskTypeHandler = taskTypeFactory.getTaskTypeHandler(taskEntity.getTaskType().name());

        taskTypeHandler.createTaskType(taskEntity.getId(), request);

        return TaskMapper.INSTANCE.toDetail(taskEntity);
    }

    @Override
    @Transactional
    public void update(UUID id, TaskUpdateRequest request) {

        userService.verifyUserExist(request.getUserId(), MessageCode.UPDATE_TASK_USER_NOT_FOUND);

        TaskEntity taskEntity = taskService.findByIdForUpdateOrThrow(id);

        TaskMapper.INSTANCE.updateEntity(request, taskEntity);

        taskService.save(taskEntity);

        TaskTypeHandler taskTypeHandler = taskTypeFactory.getTaskTypeHandler(taskEntity.getTaskType().name());

        taskTypeHandler.updateTaskType(taskEntity.getId(), request);
    }

    @Override
    public Paging<TaskDetailResponse> search(String keyword, TaskStatusEnum status, UUID userId, TaskTypeEnum taskType, Pageable pageable) {
        if (!StringUtils.hasLength(keyword)) {
            keyword = null;
        } else {
            keyword = SqlUtil.encodeKeyword(keyword.trim().toLowerCase());
        }

        Page<TaskDetailResponse> taskDetailResponses = taskService.search(keyword, status, userId, taskType, pageable);

        return Paging.<TaskDetailResponse>builder()
                .content(taskDetailResponses.getContent())
                .totalElements(taskDetailResponses.getTotalElements())
                .pageNumber(taskDetailResponses.getNumber())
                .pageSize(taskDetailResponses.getSize())
                .build();
    }

    @Override
    public TaskDetailResponse get(UUID id) {
        return taskService.findTaskDetailResponseByIdOrThrow(id);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        TaskEntity taskEntity = taskService.findByIdForUpdateOrThrow(id);
        TaskTypeHandler taskTypeHandler = taskTypeFactory.getTaskTypeHandler(taskEntity.getTaskType().name());

        taskTypeHandler.deleteTaskType(id);

        taskService.delete(taskEntity.getId());
    }
}
