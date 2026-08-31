package com.javalive.backend.repository;

import com.javalive.backend.entity.Investment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InvestmentRepository extends JpaRepository<Investment, Long> {

    List<Investment> findByUserId(Long userId);

    List<Investment> findByUserIdOrderByIdDesc(Long userId);

    List<Investment> findByUserIdAndActiveOrderByIdDesc(Long userId, String active);

    List<Investment> findByPlanId(Long planId);

    @org.springframework.data.jpa.repository.Query("select i from Investment i join fetch i.user join fetch i.plan order by i.id desc")
    List<Investment> findAllWithUserAndPlanOrderByIdDesc();

    @org.springframework.data.jpa.repository.Query("select i from Investment i join fetch i.user join fetch i.plan where i.active = :active order by i.id desc")
    List<Investment> findByActiveWithUserAndPlanOrderByIdDesc(@org.springframework.data.repository.query.Param("active") String active);
}
