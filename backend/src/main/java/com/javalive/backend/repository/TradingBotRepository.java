package com.javalive.backend.repository;

import com.javalive.backend.entity.TradingBot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TradingBotRepository extends JpaRepository<TradingBot, Long> {

    List<TradingBot> findByStatus(String status);

    List<TradingBot> findAllByOrderByCreatedAtDesc();

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    long countByStatus(String status);
}
