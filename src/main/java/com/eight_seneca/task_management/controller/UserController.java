package com.eight_seneca.task_management.controller;

import com.eight_seneca.common.factory.BaseController;
import com.eight_seneca.common.factory.BaseResponse;
import com.eight_seneca.common.factory.Paging;
import com.eight_seneca.common.util.PageCriteria;
import com.eight_seneca.common.util.PageCriteriaPageableMapper;
import com.eight_seneca.task_management.applicationservice.UserApplicationService;
import com.eight_seneca.task_management.controller.request.TaskCreateRequest;
import com.eight_seneca.task_management.controller.request.UserCreateRequest;
import com.eight_seneca.task_management.controller.request.UserUpdateRequest;
import com.eight_seneca.task_management.controller.response.UserDetailResponse;
import com.eight_seneca.task_management.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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
@RequestMapping("/v1/user")
@RequiredArgsConstructor
public class UserController extends BaseController {

    private final UserApplicationService userApplicationService;

    private final PageCriteriaPageableMapper pageCriteriaPageableMapper;


    @PostMapping
    public ResponseEntity<BaseResponse<UserDetailResponse>> create(@RequestBody @Valid UserCreateRequest request) {
        return success(userApplicationService.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<UserDetailResponse>> get(@PathVariable UUID id) {
        return success(userApplicationService.get(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BaseResponse<Void>> update(@PathVariable UUID id, @RequestBody UserUpdateRequest request) {
        userApplicationService.update(id, request);
        return success();
    }

    @GetMapping("/search")
    public ResponseEntity<BaseResponse<Paging<UserDetailResponse>>> search(@RequestParam(value = "keyword", required = false) String keyword,
                                                                           @Valid PageCriteria pageable) {
        return success(
                userApplicationService.search(keyword, pageCriteriaPageableMapper.toPageable(pageable)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse<Void>> delete(@PathVariable UUID id) {
        userApplicationService.delete(id);
        return success();
    }
}
