package com.javalive.backend.repository;

import com.javalive.backend.entity.MarketPrice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface MarketPriceRepository extends JpaRepository<MarketPrice, Long> {

    Optional<MarketPrice> findByInstrumentIdAndRecordedAtAndPriceInterval(Long instrumentId, LocalDateTime recordedAt, String priceInterval);
}
