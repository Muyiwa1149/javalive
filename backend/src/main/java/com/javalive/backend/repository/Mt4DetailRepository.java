package com.javalive.backend.repository;

import com.javalive.backend.entity.Mt4Detail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface Mt4DetailRepository extends JpaRepository<Mt4Detail, Long> {

    List<Mt4Detail> findByUserId(Long userId);

    List<Mt4Detail> findByUserIdOrderByIdDesc(Long userId);

    Optional<Mt4Detail> findByIdAndUserId(Long id, Long userId);

    long countByUserId(Long userId);
}
