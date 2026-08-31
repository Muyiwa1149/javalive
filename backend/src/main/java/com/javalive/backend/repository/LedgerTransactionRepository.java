package com.javalive.backend.repository;

import com.javalive.backend.entity.LedgerTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

public interface LedgerTransactionRepository extends JpaRepository<LedgerTransaction, Long> {

    @Query("select coalesce(sum(t.amount), 0) from LedgerTransaction t")
    BigDecimal sumAmount();

    List<LedgerTransaction> findByUserId(Long userId);

    List<LedgerTransaction> findByUserIdOrderByIdDesc(Long userId);

    List<LedgerTransaction> findByUserIdAndType(Long userId, String type);

    List<LedgerTransaction> findTop5ByUserIdAndTypeInOrderByIdDesc(Long userId, List<String> types);

    List<LedgerTransaction> findByInvestmentIdAndTypeInOrderByIdDesc(Long investmentId, List<String> types);

    List<LedgerTransaction> findByUserIdAndLeverageIsNullOrderByIdDesc(Long userId);

    List<LedgerTransaction> findByUserIdAndTypeInOrderByIdDesc(Long userId, List<String> types);
}
