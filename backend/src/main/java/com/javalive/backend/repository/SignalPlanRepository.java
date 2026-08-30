package com.javalive.backend.repository;

import com.javalive.backend.entity.SignalPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SignalPlanRepository extends JpaRepository<SignalPlan, Long> {

    List<SignalPlan> findByType(String type);
}
