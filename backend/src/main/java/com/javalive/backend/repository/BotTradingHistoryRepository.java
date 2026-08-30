package com.javalive.backend.repository;

import com.javalive.backend.entity.BotTradingHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BotTradingHistoryRepository extends JpaRepository<BotTradingHistory, Long> {

    List<BotTradingHistory> findByUserBotInvestmentIdOrderByOpenedAtDesc(Long userBotInvestmentId);

    List<BotTradingHistory> findByUserBotInvestmentUserIdOrderByOpenedAtDesc(Long userId);
}
