package com.eight_seneca.task_management.controller.request;


import com.eight_seneca.task_management.enums.TaskStatusEnum;
import com.eight_seneca.task_management.enums.TaskTypeEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class TaskUpdateRequest {

    @NotBlank
    private String name;

    private String description;

    private TaskStatusEnum status;

    @NotNull
    private UUID userId;

    private String severity;

    private String stepsToReproduce;

    private Integer businessValue;

    private Instant deadline;

}
