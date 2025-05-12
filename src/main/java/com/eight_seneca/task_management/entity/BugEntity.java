package com.eight_seneca.task_management.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "bug")
public class BugEntity {

    @Id
    private UUID taskId;

    @Column(nullable = false, length = 50)
    private String severity;

    @Column(name = "steps_to_reproduce", columnDefinition = "TEXT", nullable = false)
    private String stepsToReproduce;

    // Getters and setters
}
