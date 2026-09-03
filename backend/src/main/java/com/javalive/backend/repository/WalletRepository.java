package com.javalive.backend.repository;

import com.javalive.backend.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WalletRepository extends JpaRepository<Wallet, Long> {

    List<Wallet> findByUserId(Long userId);

    @org.springframework.data.jpa.repository.Query("select w from Wallet w join fetch w.user order by w.id desc")
    List<Wallet> findAllWithUserOrderByIdDesc();
}
