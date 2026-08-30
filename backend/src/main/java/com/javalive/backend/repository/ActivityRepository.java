package com.javalive.backend.repository;

import com.javalive.backend.entity.Activity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityRepository extends JpaRepository<Activity, Long> {

    List<Activity> findByUser_Id(Long userId);
}
