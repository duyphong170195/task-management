package com.eight_seneca.task_management.repository;

import com.eight_seneca.task_management.controller.response.TaskDetailResponse;
import com.eight_seneca.task_management.entity.TaskEntity;
import com.eight_seneca.task_management.enums.TaskStatusEnum;
import com.eight_seneca.task_management.enums.TaskTypeEnum;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<TaskEntity, UUID> {

    @Query("SELECT new com.eight_seneca.task_management.controller.response.TaskDetailResponse(te.id, te.name, te.description, te.taskType, te.status, te.userId, be.severity, be.stepsToReproduce, fe.businessValue, fe.deadline) "
            + " FROM TaskEntity te "
            + " LEFT JOIN BugEntity be ON te.id = be.taskId "
            + " LEFT JOIN FeatureEntity fe ON te.id = fe.taskId "
            + " WHERE ((COALESCE(:keyword, NULL) IS NULL)  OR (LOWER(te.name) LIKE %:keyword%) "
            + " OR (LOWER(te.description) LIKE %:keyword%))  "
            + " AND ((COALESCE(:status, NULL) IS NULL) OR (te.status = :status)) "
            + " AND ((COALESCE(:userId, NULL) IS NULL) OR (te.userId = :userId)) "
            + " AND ((COALESCE(:taskType, NULL) IS NULL) OR (te.taskType = :taskType)) "
    )
    Page<TaskDetailResponse> search(@Param("keyword") String keyword,
                                    @Param("status") TaskStatusEnum status,
                                    @Param("userId") UUID userId,
                                    @Param("taskType") TaskTypeEnum taskType,
                                    Pageable pageable);

    @Query("SELECT new com.eight_seneca.task_management.controller.response.TaskDetailResponse(te.id, te.name, te.description, te.taskType, te.status, te.userId, be.severity, be.stepsToReproduce, fe.businessValue, fe.deadline) "
            + " FROM TaskEntity te "
            + " LEFT JOIN BugEntity be ON te.id = be.taskId "
            + " LEFT JOIN FeatureEntity fe ON te.id = fe.taskId "
            + " WHERE te.id = :id "
    )
    Optional<TaskDetailResponse> findTaskDetailResponseById(UUID id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "10000")})
    @Query("SELECT t FROM TaskEntity t where t.id = :id ")
    Optional<TaskEntity> findByIdForUpdate(UUID id);
}
