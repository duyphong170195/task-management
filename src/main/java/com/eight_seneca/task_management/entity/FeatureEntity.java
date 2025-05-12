package com.eight_seneca.task_management.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "feature")
public class FeatureEntity {

    @Id
    private UUID taskId;

    @Column(nullable = false, length = 50)
    private Integer businessValue;

    @Column(name = "deadline", columnDefinition = "TEXT", nullable = false)
    private Instant deadline;

    // Getters and setters
}
