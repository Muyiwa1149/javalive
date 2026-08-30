package com.javalive.backend.repository;

import com.javalive.backend.entity.CopyTradingExpert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CopyTradingExpertRepository extends JpaRepository<CopyTradingExpert, Long> {

    List<CopyTradingExpert> findByStatusOrderByRatingDescWinRateDesc(String status);
}
