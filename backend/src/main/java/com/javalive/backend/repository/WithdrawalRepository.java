package com.javalive.backend.repository;

import com.javalive.backend.entity.Withdrawal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface WithdrawalRepository extends JpaRepository<Withdrawal, Long> {

    List<Withdrawal> findByUserId(Long userId);

    List<Withdrawal> findByUserIdOrderByIdDesc(Long userId);

    List<Withdrawal> findByStatus(String status);

    List<Withdrawal> findByStatusOrderByIdDesc(String status);

    /** Native SQL + inner join for the same reason as {@code DepositRepository.sumAmountByStatus} — see its javadoc. */
    @Query(value = "select coalesce(sum(w.amount), 0) from withdrawals w inner join users u on u.id = w.user_id "
            + "where w.status = :status", nativeQuery = true)
    BigDecimal sumAmountByStatus(@Param("status") String status);

    /** Inner join deliberately excludes rows whose user_id no longer resolves (orphaned migration data). */
    @Query("select w from Withdrawal w join fetch w.user order by w.id desc")
    List<Withdrawal> findAllWithUserOrderByIdDesc();

    @Query("select w from Withdrawal w join fetch w.user where w.status = :status order by w.id desc")
    List<Withdrawal> findByStatusWithUserOrderByIdDesc(@Param("status") String status);
}
