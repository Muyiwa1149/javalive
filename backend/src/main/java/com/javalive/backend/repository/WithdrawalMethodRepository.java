package com.javalive.backend.repository;

import com.javalive.backend.entity.WithdrawalMethod;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WithdrawalMethodRepository extends JpaRepository<WithdrawalMethod, Long> {

    List<WithdrawalMethod> findByType(String type);
}
