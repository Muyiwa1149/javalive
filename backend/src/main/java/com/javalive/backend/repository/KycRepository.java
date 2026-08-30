package com.javalive.backend.repository;

import com.javalive.backend.entity.Kyc;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface KycRepository extends JpaRepository<Kyc, Long> {

    List<Kyc> findByUserId(Long userId);

    List<Kyc> findByStatus(String status);

    Optional<Kyc> findTopByUserIdOrderByIdDesc(Long userId);
}
