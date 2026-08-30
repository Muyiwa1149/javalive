package com.javalive.backend.repository;

import com.javalive.backend.entity.UserSignalPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserSignalPlanRepository extends JpaRepository<UserSignalPlan, Long> {

    List<UserSignalPlan> findByUserIdOrderByIdDesc(Long userId);

    List<UserSignalPlan> findByUserIdAndStatusOrderByIdDesc(Long userId, String status);
}
