package com.javalive.backend.repository;

import com.javalive.backend.entity.TradingBot;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TradingBotRepository extends JpaRepository<TradingBot, Long> {
}
