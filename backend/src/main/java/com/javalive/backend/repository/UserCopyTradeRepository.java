package com.javalive.backend.repository;

import com.javalive.backend.entity.UserCopyTrade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface UserCopyTradeRepository extends JpaRepository<UserCopyTrade, Long> {

    List<UserCopyTrade> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<UserCopyTrade> findByUserIdAndActive(Long userId, String active);

    Optional<UserCopyTrade> findByUserIdAndExpertIdAndActive(Long userId, Long expertId, String active);

    long countByActive(String active);

    long countByExpertId(Long expertId);

    long countByExpertIdAndActive(Long expertId, String active);

    /** Join-safe variant used for the delete-guard and per-expert card count — see the note above. */
    @Query(value = "select count(*) from user_copy_trades t inner join users u on u.id = t.user_id "
            + "where t.expert_id = :expertId and t.active = :active", nativeQuery = true)
    long countResolvableByExpertIdAndActive(@Param("expertId") Long expertId, @Param("active") String active);

    /**
     * These aggregates deliberately inner-join user+expert (not a plain derived count/sum) so they
     * stay consistent with {@link #findByActiveWithUserAndExpertOrderByCreatedAtDesc} — a raw count
     * would include orphaned migration rows (FK pointing at a since-deleted user) that the list view
     * can never actually display, producing a stat that doesn't match what's shown on screen.
     */
    @Query(value = "select count(*) from user_copy_trades t inner join users u on u.id = t.user_id "
            + "inner join copy_trading_experts e on e.id = t.expert_id where t.active = :active", nativeQuery = true)
    long countResolvableByActive(@Param("active") String active);

    @Query(value = "select count(distinct t.user_id) from user_copy_trades t inner join users u on u.id = t.user_id "
            + "where t.active = :active", nativeQuery = true)
    long countDistinctUsersByActive(@Param("active") String active);

    @Query(value = "select coalesce(sum(t.price), 0) from user_copy_trades t inner join users u on u.id = t.user_id "
            + "inner join copy_trading_experts e on e.id = t.expert_id where t.active = :active", nativeQuery = true)
    BigDecimal sumPriceByActive(@Param("active") String active);

    @Query(value = "select coalesce(sum(t.total_profit), 0) from user_copy_trades t inner join users u on u.id = t.user_id "
            + "inner join copy_trading_experts e on e.id = t.expert_id where t.active = :active", nativeQuery = true)
    BigDecimal sumTotalProfitByActive(@Param("active") String active);

    @Query("select t from UserCopyTrade t join fetch t.user join fetch t.expert where t.active = :active order by t.createdAt desc")
    List<UserCopyTrade> findByActiveWithUserAndExpertOrderByCreatedAtDesc(@Param("active") String active);
}
