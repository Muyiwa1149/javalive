package com.javalive.backend.repository;

import com.javalive.backend.entity.UserPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserPlanRepository extends JpaRepository<UserPlan, Long> {

    List<UserPlan> findByUserId(Long userId);

    List<UserPlan> findByPlanId(Long planId);
}
