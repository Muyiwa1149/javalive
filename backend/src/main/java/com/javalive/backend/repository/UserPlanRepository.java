package com.javalive.backend.repository;

import com.javalive.backend.entity.UserPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserPlanRepository extends JpaRepository<UserPlan, Long> {

    List<UserPlan> findByUserId(Long userId);

    List<UserPlan> findByPlanId(Long planId);

    List<UserPlan> findTop2ByUserIdAndActiveOrderByIdDesc(Long userId, String active);

    List<UserPlan> findByUserIdAndAssetSymbolAndActiveOrderByCreatedAtDesc(Long userId, String assetSymbol, String active);

    List<UserPlan> findByUserIdAndAssetSymbolOrderByCreatedAtDesc(Long userId, String assetSymbol);

    Optional<UserPlan> findByIdAndUserId(Long id, Long userId);
}
