package com.javalive.backend.repository;

import com.javalive.backend.entity.Kyc;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface KycRepository extends JpaRepository<Kyc, Long> {

    List<Kyc> findByUserId(Long userId);

    List<Kyc> findByStatus(String status);
}
