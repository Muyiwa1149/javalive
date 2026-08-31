package com.javalive.backend.repository;

import com.javalive.backend.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    @Query("select t from Task t join fetch t.assignedToAdmin order by t.id desc")
    List<Task> findAllWithAdminOrderByIdDesc();

    @Query("select t from Task t join fetch t.assignedToAdmin a where a.id = :adminId order by t.id desc")
    List<Task> findByAssignedToAdminIdOrderByIdDesc(@org.springframework.data.repository.query.Param("adminId") Long adminId);
}
