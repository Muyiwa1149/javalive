package com.javalive.backend.repository;

import com.javalive.backend.entity.LedgerTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LedgerTransactionRepository extends JpaRepository<LedgerTransaction, Long> {

    List<LedgerTransaction> findByUserId(Long userId);

    List<LedgerTransaction> findByUserIdAndType(Long userId, String type);
}
