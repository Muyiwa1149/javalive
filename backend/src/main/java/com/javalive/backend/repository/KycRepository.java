package com.javalive.backend.repository;

import com.javalive.backend.entity.Kyc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface KycRepository extends JpaRepository<Kyc, Long> {

    List<Kyc> findByUserId(Long userId);

    /** Inner join deliberately excludes rows whose user_id no longer resolves (orphaned migration data). */
    @Query("select k from Kyc k join fetch k.user order by k.id desc")
    List<Kyc> findAllByOrderByIdDesc();

    List<Kyc> findByStatus(String status);

    Optional<Kyc> findTopByUserIdOrderByIdDesc(Long userId);
}
