package com.javalive.backend.repository;

import com.javalive.backend.entity.CryptoAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CryptoAccountRepository extends JpaRepository<CryptoAccount, Long> {

    Optional<CryptoAccount> findByUserId(Long userId);
}
