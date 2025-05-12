package com.eight_seneca.task_management.applicationservice;

import com.eight_seneca.common.factory.Paging;
import com.eight_seneca.task_management.controller.request.TaskCreateRequest;
import com.eight_seneca.task_management.controller.request.TaskUpdateRequest;
import com.eight_seneca.task_management.controller.response.TaskDetailResponse;
import com.eight_seneca.task_management.enums.TaskStatusEnum;
import com.eight_seneca.task_management.enums.TaskTypeEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface TaskApplicationService {

    TaskDetailResponse create(TaskCreateRequest request);
    void update(UUID id, TaskUpdateRequest request);

    Paging<TaskDetailResponse> search(String keyword,
                                      TaskStatusEnum status,
                                      UUID userId,
                                      TaskTypeEnum taskType, Pageable pageable);

    TaskDetailResponse get(UUID id);

    void delete(UUID id);
}
