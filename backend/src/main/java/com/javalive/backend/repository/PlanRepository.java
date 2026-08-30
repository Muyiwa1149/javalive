package com.javalive.backend.repository;

import com.javalive.backend.entity.Plan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlanRepository extends JpaRepository<Plan, Long> {

    List<Plan> findByTypeIgnoreCaseAndActiveTrue(String type);
}
