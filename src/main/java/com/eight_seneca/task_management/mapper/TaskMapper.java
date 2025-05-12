package com.eight_seneca.task_management.mapper;

import com.eight_seneca.task_management.controller.request.TaskCreateRequest;
import com.eight_seneca.task_management.controller.request.TaskUpdateRequest;
import com.eight_seneca.task_management.controller.response.TaskDetailResponse;
import com.eight_seneca.task_management.entity.TaskEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TaskMapper {
    TaskMapper INSTANCE = Mappers.getMapper(TaskMapper.class);

    @Mapping(target = "status", expression = "java(com.eight_seneca.task_management.enums.TaskStatusEnum.OPEN)")
    TaskEntity toEntity(TaskCreateRequest request);

    TaskDetailResponse toDetail(TaskEntity TaskEntity);
//
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(TaskUpdateRequest request, @MappingTarget TaskEntity TaskEntity);

}
