package com.eight_seneca.task_management.entity;

import com.eight_seneca.common.entity.AuditEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "\"user\"")
public class UserEntity extends AuditEntity {

    @Column(name = "username",length = 50, nullable = false)
    private String username;

    @Column(name = "full_name", length = 50, nullable = false)
    private String fullName;
}
