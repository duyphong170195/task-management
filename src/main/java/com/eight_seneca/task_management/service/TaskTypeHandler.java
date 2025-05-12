package com.eight_seneca.task_management.service;

import com.eight_seneca.task_management.controller.request.TaskCreateRequest;
import com.eight_seneca.task_management.controller.request.TaskUpdateRequest;

import java.util.UUID;

public interface TaskTypeHandler {

    void createTaskType(UUID taskId, TaskCreateRequest taskCreateRequest);
    void updateTaskType(UUID taskId, TaskUpdateRequest taskUpdateRequest);
    void deleteTaskType(UUID taskId);
}
