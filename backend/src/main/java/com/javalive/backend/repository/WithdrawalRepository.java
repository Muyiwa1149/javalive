package com.javalive.backend.repository;

import com.javalive.backend.entity.Withdrawal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface WithdrawalRepository extends JpaRepository<Withdrawal, Long> {

    List<Withdrawal> findByUserId(Long userId);

    List<Withdrawal> findByUserIdOrderByIdDesc(Long userId);

    List<Withdrawal> findByStatus(String status);

    List<Withdrawal> findByStatusOrderByIdDesc(String status);

    @Query("select coalesce(sum(w.amount), 0) from Withdrawal w where w.status = :status")
    BigDecimal sumAmountByStatus(@Param("status") String status);
}
