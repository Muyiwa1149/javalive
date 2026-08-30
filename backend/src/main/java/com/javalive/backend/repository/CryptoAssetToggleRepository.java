package com.javalive.backend.repository;

import com.javalive.backend.entity.CryptoAssetToggle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CryptoAssetToggleRepository extends JpaRepository<CryptoAssetToggle, Long> {

    Optional<CryptoAssetToggle> findBySymbol(String symbol);
}
