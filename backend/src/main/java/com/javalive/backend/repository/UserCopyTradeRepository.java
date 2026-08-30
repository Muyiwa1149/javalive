package com.javalive.backend.repository;

import com.javalive.backend.entity.UserCopyTrade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserCopyTradeRepository extends JpaRepository<UserCopyTrade, Long> {

    List<UserCopyTrade> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<UserCopyTrade> findByUserIdAndActive(Long userId, String active);

    Optional<UserCopyTrade> findByUserIdAndExpertIdAndActive(Long userId, Long expertId, String active);
}
