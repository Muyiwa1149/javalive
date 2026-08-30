package com.javalive.backend.repository;

import com.javalive.backend.entity.BotTradingHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BotTradingHistoryRepository extends JpaRepository<BotTradingHistory, Long> {
}
