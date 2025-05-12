package com.eight_seneca.task_management.controller;

import com.eight_seneca.common.factory.BaseController;
import com.eight_seneca.common.factory.BaseResponse;
import com.eight_seneca.common.factory.Paging;
import com.eight_seneca.common.util.PageCriteria;
import com.eight_seneca.common.util.PageCriteriaPageableMapper;
import com.eight_seneca.task_management.applicationservice.TaskApplicationService;
import com.eight_seneca.task_management.controller.request.TaskCreateRequest;
import com.eight_seneca.task_management.controller.request.TaskUpdateRequest;
import com.eight_seneca.task_management.controller.request.UserUpdateRequest;
import com.eight_seneca.task_management.controller.response.TaskDetailResponse;
import com.eight_seneca.task_management.controller.response.UserDetailResponse;
import com.eight_seneca.task_management.enums.TaskStatusEnum;
import com.eight_seneca.task_management.enums.TaskTypeEnum;
import com.eight_seneca.task_management.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/v1/task")
@RequiredArgsConstructor
public class TaskController extends BaseController {

    private final TaskApplicationService applicationService;

    private final PageCriteriaPageableMapper pageCriteriaPageableMapper;

    @PostMapping
    public ResponseEntity<BaseResponse<TaskDetailResponse>> create(@RequestBody @Valid TaskCreateRequest request) {
        return success(applicationService.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<TaskDetailResponse>> get(@PathVariable UUID id) {
        return success(applicationService.get(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BaseResponse<Void>> update(@PathVariable UUID id, @RequestBody TaskUpdateRequest request) {
        applicationService.update(id, request);
        return success();
    }
    @GetMapping("/search")
    public ResponseEntity<BaseResponse<Paging<TaskDetailResponse>>> search(@RequestParam(value = "keyword", required = false) String keyword,
                                                                           @RequestParam(value = "status", required = false) TaskStatusEnum status,
                                                                           @RequestParam(value = "userId", required = false) UUID userId,
                                                                           @RequestParam(value = "taskType", required = false) TaskTypeEnum taskType,
                                                                           @Valid PageCriteria pageable) {
        return success(
                applicationService.search(keyword, status, userId, taskType, pageCriteriaPageableMapper.toPageable(pageable)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse<Void>> delete(@PathVariable UUID id) {
        applicationService.delete(id);
        return success();
    }
}
