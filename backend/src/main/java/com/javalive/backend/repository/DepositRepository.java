package com.javalive.backend.repository;

import com.javalive.backend.entity.Deposit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface DepositRepository extends JpaRepository<Deposit, Long> {

    List<Deposit> findByUserId(Long userId);

    List<Deposit> findByUserIdOrderByIdDesc(Long userId);

    List<Deposit> findByStatus(String status);

    List<Deposit> findByStatusOrderByIdDesc(String status);

    @Query("select coalesce(sum(d.amount), 0) from Deposit d where d.status = :status")
    BigDecimal sumAmountByStatus(@Param("status") String status);

    @Query("select distinct d.user.id from Deposit d")
    List<Long> findDistinctUserIds();
}
