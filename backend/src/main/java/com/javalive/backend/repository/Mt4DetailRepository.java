package com.javalive.backend.repository;

import com.javalive.backend.entity.Mt4Detail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface Mt4DetailRepository extends JpaRepository<Mt4Detail, Long> {

    List<Mt4Detail> findByUserId(Long userId);

    long countByUserId(Long userId);
}
