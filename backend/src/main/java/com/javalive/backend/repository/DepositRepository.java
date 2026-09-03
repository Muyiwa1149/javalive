package com.javalive.backend.repository;

import com.javalive.backend.entity.Deposit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface DepositRepository extends JpaRepository<Deposit, Long> {

    List<Deposit> findByUserId(Long userId);

    List<Deposit> findByUserIdOrderByIdDesc(Long userId);

    List<Deposit> findByStatus(String status);

    List<Deposit> findByStatusOrderByIdDesc(String status);

    /** Inner join deliberately excludes rows whose user_id no longer resolves (orphaned migration data). */
    @Query("select d from Deposit d join fetch d.user order by d.id desc")
    List<Deposit> findAllWithUserOrderByIdDesc();

    @Query("select d from Deposit d join fetch d.user where d.status = :status order by d.id desc")
    List<Deposit> findByStatusWithUserOrderByIdDesc(@Param("status") String status);

    /**
     * Native SQL + inner join, deliberately not a JPQL {@code sum()} projection — a plain (non-fetch)
     * JPQL join inside a count()/sum() projection was found not to filter reliably in this Hibernate
     * version (see the Copy Trading admin module). Inner-joining keeps this consistent with
     * {@link #findAllWithUserOrderByIdDesc} so the dashboard total can never disagree with what the
     * admin list actually shows for orphaned migration rows (a real, sizeable case here — see the
     * parity checklist).
     */
    @Query(value = "select coalesce(sum(d.amount), 0) from deposits d inner join users u on u.id = d.user_id "
            + "where d.status = :status", nativeQuery = true)
    BigDecimal sumAmountByStatus(@Param("status") String status);

    @Query("select distinct d.user.id from Deposit d")
    List<Long> findDistinctUserIds();
}
