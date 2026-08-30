package com.javalive.backend.repository;

import com.javalive.backend.entity.Investment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InvestmentRepository extends JpaRepository<Investment, Long> {

    List<Investment> findByUserId(Long userId);

    List<Investment> findByUserIdOrderByIdDesc(Long userId);

    List<Investment> findByUserIdAndActiveOrderByIdDesc(Long userId, String active);

    List<Investment> findByPlanId(Long planId);
}
