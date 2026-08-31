package com.javalive.backend.repository;

import com.javalive.backend.entity.Mt4Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface Mt4SubscriptionRepository extends JpaRepository<Mt4Subscription, Long> {

    List<Mt4Subscription> findByUserId(Long userId);
}
