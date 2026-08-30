package com.javalive.backend.repository;

import com.javalive.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    Optional<User> findByEmailOrUsername(String email, String username);

    java.util.List<User> findByReferredByCode(String referredByCode);
}
