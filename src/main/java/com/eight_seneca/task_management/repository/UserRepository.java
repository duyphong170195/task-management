package com.eight_seneca.task_management.repository;

import com.eight_seneca.task_management.entity.TaskEntity;
import com.eight_seneca.task_management.entity.UserEntity;
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

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, UUID> {

    @Query("SELECT u "
            + " FROM UserEntity u "
            + " WHERE ((COALESCE(:keyword, NULL) IS NULL)  OR (LOWER(u.fullName) LIKE %:keyword%) "
            + " OR (LOWER(u.username) LIKE %:keyword%))  "
    )
    Page<UserEntity> search(@Param("keyword") String keyword,
                            Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "10000")})
    @Query("SELECT u FROM UserEntity u where u.id = :id ")
    Optional<UserEntity> findByIdForUpdate(UUID id);

    @Query("SELECT COUNT(u) FROM UserEntity u WHERE u.username = :username")
    long countByUsername(String username);

}
