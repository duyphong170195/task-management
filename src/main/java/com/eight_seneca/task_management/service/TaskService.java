package com.eight_seneca.task_management.service;

import com.eight_seneca.task_management.controller.response.TaskDetailResponse;
import com.eight_seneca.task_management.entity.TaskEntity;
import com.eight_seneca.task_management.enums.TaskStatusEnum;
import com.eight_seneca.task_management.enums.TaskTypeEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface TaskService {

    TaskEntity save(TaskEntity taskEntity);

    TaskEntity findByIdForUpdateOrThrow(UUID id);

    Page<TaskDetailResponse> search(String keyword,
                                    TaskStatusEnum status,
                                    UUID userId,
                                    TaskTypeEnum taskType, Pageable pageable);

    void delete(UUID id);

    void verifyTaskExist(UUID id);

    TaskDetailResponse findTaskDetailResponseByIdOrThrow(UUID id);
}
