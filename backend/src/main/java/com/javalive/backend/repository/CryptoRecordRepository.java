package com.javalive.backend.repository;

import com.javalive.backend.entity.CryptoRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CryptoRecordRepository extends JpaRepository<CryptoRecord, Long> {

    List<CryptoRecord> findByUserId(Long userId);
}
