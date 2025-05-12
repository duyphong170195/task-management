package com.eight_seneca.task_management.service.impl;

import com.eight_seneca.task_management.controller.request.TaskCreateRequest;
import com.eight_seneca.task_management.controller.request.TaskUpdateRequest;
import com.eight_seneca.task_management.entity.BugEntity;
import com.eight_seneca.task_management.mapper.BugMapper;
import com.eight_seneca.task_management.repository.BugRepository;
import com.eight_seneca.task_management.service.TaskTypeHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service("BUG")
@RequiredArgsConstructor
public class BugServiceImpl implements TaskTypeHandler {

    private final BugRepository repository;

    @Override
    @Transactional
    public void createTaskType(UUID taskId, TaskCreateRequest taskCreateRequest) {

        BugEntity bugEntity = BugMapper.INSTANCE.toEntity(taskCreateRequest);
        bugEntity.setTaskId(taskId);

        repository.save(bugEntity);
    }

    @Override
    @Transactional
    public void updateTaskType(UUID taskId, TaskUpdateRequest taskUpdateRequest) {

        BugEntity bugEntity = repository.findById(taskId).orElseThrow();

        BugMapper.INSTANCE.updateEntity(taskUpdateRequest, bugEntity);

        repository.save(bugEntity);
    }

    @Override
    @Transactional
    public void deleteTaskType(UUID taskId) {
        repository.deleteById(taskId);
    }
}
