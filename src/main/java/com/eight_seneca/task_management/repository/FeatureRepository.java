package com.eight_seneca.task_management.repository;

import com.eight_seneca.task_management.entity.FeatureEntity;
import com.eight_seneca.task_management.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface FeatureRepository extends JpaRepository<FeatureEntity, UUID> {
}
