package com.javalive.backend.repository;

import com.javalive.backend.entity.Withdrawal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WithdrawalRepository extends JpaRepository<Withdrawal, Long> {

    List<Withdrawal> findByUserId(Long userId);

    List<Withdrawal> findByUserIdOrderByIdDesc(Long userId);

    List<Withdrawal> findByStatus(String status);

    List<Withdrawal> findByStatusOrderByIdDesc(String status);
}
