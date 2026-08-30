package com.javalive.backend.repository;

import com.javalive.backend.entity.MarketPrice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MarketPriceRepository extends JpaRepository<MarketPrice, Long> {
}
