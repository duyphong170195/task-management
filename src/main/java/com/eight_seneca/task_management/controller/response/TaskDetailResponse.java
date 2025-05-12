package com.eight_seneca.task_management.controller.response;

import com.eight_seneca.task_management.enums.TaskStatusEnum;
import com.eight_seneca.task_management.enums.TaskTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskDetailResponse {

    private UUID id;

    private String name;

    private String description;

    private TaskTypeEnum taskType;

    private TaskStatusEnum status;

    private UUID userId;

    private String severity;

    private String stepsToReproduce;

    private Integer businessValue;

    private Instant deadline;
}
