package com.javalive.backend.repository;

import com.javalive.backend.entity.Testimony;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestimonyRepository extends JpaRepository<Testimony, Long> {
}
