package com.javalive.backend.repository;

import com.javalive.backend.entity.UserBotInvestment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserBotInvestmentRepository extends JpaRepository<UserBotInvestment, Long> {

    List<UserBotInvestment> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<UserBotInvestment> findByUserIdAndBotIdAndStatus(Long userId, Long botId, String status);
}
