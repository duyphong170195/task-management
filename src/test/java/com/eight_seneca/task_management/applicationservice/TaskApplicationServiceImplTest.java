package com.eight_seneca.task_management.applicationservice;

import com.eight_seneca.common.factory.Paging;
import com.eight_seneca.task_management.applicationservice.impl.TaskApplicationServiceImpl;
import com.eight_seneca.task_management.controller.request.TaskCreateRequest;
import com.eight_seneca.task_management.controller.request.TaskUpdateRequest;
import com.eight_seneca.task_management.controller.response.TaskDetailResponse;
import com.eight_seneca.task_management.entity.TaskEntity;
import com.eight_seneca.task_management.enums.TaskStatusEnum;
import com.eight_seneca.task_management.enums.TaskTypeEnum;
import com.eight_seneca.task_management.fixture.task.TaskCreateRequestFixtures;
import com.eight_seneca.task_management.fixture.task.TaskUpdateRequestFixtures;
import com.eight_seneca.task_management.mapper.TaskMapper;
import com.eight_seneca.task_management.service.TaskService;
import com.eight_seneca.task_management.service.TaskTypeHandler;
import com.eight_seneca.task_management.service.UserService;
import com.eight_seneca.task_management.service.factory.TaskTypeFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class TaskApplicationServiceImplTest {

    @InjectMocks
    private TaskApplicationServiceImpl taskApplicationService;

    @Mock
    private TaskService taskService;

    @Mock
    private UserService userService;

    @Mock
    private TaskTypeFactory taskTypeFactory;

    @Mock
    private TaskTypeHandler taskTypeHandler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void test__createBug__success() {
        // Arrange
        TaskCreateRequest request = TaskCreateRequestFixtures.createBugRequest();

        TaskEntity taskEntity = TaskMapper.INSTANCE.toEntity(request);
        taskEntity.setId(UUID.randomUUID());

        when(taskService.save(any())).thenReturn(taskEntity);
        when(taskTypeFactory.getTaskTypeHandler(request.getTaskType().name())).thenReturn(taskTypeHandler);

        // Act
        taskApplicationService.create(request);

        // Assert
        verify(userService).verifyUserExist(eq(request.getUserId()), any());

        ArgumentCaptor<TaskEntity> argumentCaptor = ArgumentCaptor.forClass(TaskEntity.class);

        verify(taskService).save(argumentCaptor.capture());
        verify(taskTypeHandler).createTaskType(eq(taskEntity.getId()), eq(request));

        this.verifyCapturedForCreateTask(argumentCaptor.getValue(), taskEntity);
    }

    @Test
    void test__createFeature__success() {
        // Arrange
        TaskCreateRequest request = TaskCreateRequestFixtures.createFeatureRequest();

        TaskEntity taskEntity = TaskMapper.INSTANCE.toEntity(request);
        taskEntity.setId(UUID.randomUUID());

        when(taskService.save(any())).thenReturn(taskEntity);
        when(taskTypeFactory.getTaskTypeHandler(request.getTaskType().name())).thenReturn(taskTypeHandler);

        // Act
        taskApplicationService.create(request);

        // Assert
        verify(userService).verifyUserExist(eq(request.getUserId()), any());

        ArgumentCaptor<TaskEntity> argumentCaptor = ArgumentCaptor.forClass(TaskEntity.class);

        verify(taskService).save(argumentCaptor.capture());
        verify(taskTypeHandler).createTaskType(eq(taskEntity.getId()), eq(request));

        this.verifyCapturedForCreateTask(argumentCaptor.getValue(), taskEntity);
    }


    @Test
    void test__updateTask__success() {
        UUID id = UUID.randomUUID();

        TaskCreateRequest taskCreateRequest = TaskCreateRequestFixtures.createBugRequest();
        TaskEntity taskCreated = TaskMapper.INSTANCE.toEntity(taskCreateRequest);
        taskCreated.setId(id);

        TaskUpdateRequest taskUpdateRequest = TaskUpdateRequestFixtures.createBugRequest(UUID.randomUUID());

        TaskEntity updatedTask = TaskMapper.INSTANCE.toEntity(taskCreateRequest);
        updatedTask.setId(id);

        TaskMapper.INSTANCE.updateEntity(taskUpdateRequest, updatedTask);

        when(taskService.findByIdForUpdateOrThrow(any())).thenReturn(taskCreated);
        when(taskService.save(any())).thenReturn(updatedTask);
        when(taskTypeFactory.getTaskTypeHandler(updatedTask.getTaskType().name())).thenReturn(taskTypeHandler);

        // Act
        taskApplicationService.update(id, taskUpdateRequest);

        // Assert
        verify(userService).verifyUserExist(eq(taskUpdateRequest.getUserId()), any());
        ArgumentCaptor<TaskEntity> argumentCaptor = ArgumentCaptor.forClass(TaskEntity.class);
        verify(taskService).save(argumentCaptor.capture());
        verify(taskTypeHandler).updateTaskType(eq(id), eq(taskUpdateRequest));

        this.verifyCapturedForUpdateTask(argumentCaptor.getValue(), updatedTask);
    }

    @Test
    void test__search__shouldReturnPaging() {
        String keyword = "test";
        TaskStatusEnum status = TaskStatusEnum.OPEN;
        UUID userId = UUID.randomUUID();
        TaskTypeEnum taskType = TaskTypeEnum.FEATURE;

        Pageable pageable = PageRequest.of(0, 10);
        List<TaskDetailResponse> content = List.of(new TaskDetailResponse());
        Page<TaskDetailResponse> mockPage = new PageImpl<>(content, pageable, 1);

        when(taskService.search(any(), eq(status), eq(userId), eq(taskType), eq(pageable)))
                .thenReturn(mockPage);

        Paging<TaskDetailResponse> result = taskApplicationService.search(keyword, status, userId, taskType, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getPageNumber()).isEqualTo(0);
        assertThat(result.getPageSize()).isEqualTo(10);
    }

    @Test
    void test__get__shouldReturnTaskDetail() {
        UUID id = UUID.randomUUID();
        TaskDetailResponse expected = new TaskDetailResponse();

        when(taskService.findTaskDetailResponseByIdOrThrow(id)).thenReturn(expected);

        TaskDetailResponse actual = taskApplicationService.get(id);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void testDelete_shouldCallHandlers() {
        UUID id = UUID.randomUUID();
        TaskEntity entity = new TaskEntity();
        entity.setId(id);
        entity.setTaskType(TaskTypeEnum.BUG);

        when(taskService.findByIdForUpdateOrThrow(id)).thenReturn(entity);
        when(taskTypeFactory.getTaskTypeHandler("BUG")).thenReturn(taskTypeHandler);

        taskApplicationService.delete(id);

        verify(taskTypeHandler).deleteTaskType(id);
        verify(taskService).delete(id);
    }

    private void verifyCapturedForCreateTask(TaskEntity captorActual, TaskEntity expected) {
        Assertions.assertNull(captorActual.getId());
        Assertions.assertNotNull(expected.getId());
        Assertions.assertEquals(captorActual.getTaskType(), expected.getTaskType());
        Assertions.assertEquals(captorActual.getName(), expected.getName());
        Assertions.assertEquals(captorActual.getStatus(), expected.getStatus());
        Assertions.assertEquals(captorActual.getDescription(), expected.getDescription());
    }
    private void verifyCapturedForUpdateTask(TaskEntity captorActual, TaskEntity expected) {
        Assertions.assertNotNull(captorActual.getId());
        Assertions.assertNotNull(expected.getId());
        Assertions.assertEquals(captorActual.getTaskType(), expected.getTaskType());
        Assertions.assertEquals(captorActual.getName(), expected.getName());
        Assertions.assertEquals(captorActual.getStatus(), expected.getStatus());
        Assertions.assertEquals(captorActual.getDescription(), expected.getDescription());
    }
}
