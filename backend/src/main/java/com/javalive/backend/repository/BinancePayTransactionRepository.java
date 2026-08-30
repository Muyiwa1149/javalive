package com.javalive.backend.repository;

import com.javalive.backend.entity.BinancePayTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BinancePayTransactionRepository extends JpaRepository<BinancePayTransaction, Long> {
}
