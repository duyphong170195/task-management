package com.eight_seneca.task_management.service.impl;

import com.eight_seneca.task_management.controller.request.TaskCreateRequest;
import com.eight_seneca.task_management.controller.request.TaskUpdateRequest;
import com.eight_seneca.task_management.entity.BugEntity;
import com.eight_seneca.task_management.entity.FeatureEntity;
import com.eight_seneca.task_management.entity.TaskEntity;
import com.eight_seneca.task_management.mapper.BugMapper;
import com.eight_seneca.task_management.mapper.FeatureMapper;
import com.eight_seneca.task_management.repository.FeatureRepository;
import com.eight_seneca.task_management.service.TaskTypeHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service("FEATURE")
@RequiredArgsConstructor
public class FeatureServiceImpl implements TaskTypeHandler {

    private final FeatureRepository repository;
    @Override
    @Transactional
    public void createTaskType(UUID taskId, TaskCreateRequest taskCreateRequest) {

        FeatureEntity featureEntity = FeatureMapper.INSTANCE.toEntity(taskCreateRequest);
        featureEntity.setTaskId(taskId);

        repository.save(featureEntity);
    }

    @Override
    @Transactional
    public void updateTaskType(UUID taskId, TaskUpdateRequest taskUpdateRequest) {
        FeatureEntity featureEntity = repository.findById(taskId).orElseThrow();

        FeatureMapper.INSTANCE.updateEntity(taskUpdateRequest, featureEntity);

        repository.save(featureEntity);
    }

    @Override
    @Transactional
    public void deleteTaskType(UUID taskId) {
        repository.deleteById(taskId);
    }
}
