package com.javalive.backend.repository;

import com.javalive.backend.entity.UserBotInvestment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserBotInvestmentRepository extends JpaRepository<UserBotInvestment, Long> {

    List<UserBotInvestment> findByUserIdOrderByCreatedAtDesc(Long userId);

    @Query("select i from UserBotInvestment i join fetch i.user join fetch i.bot "
            + "where i.status = :status and i.expiresAt > :now")
    List<UserBotInvestment> findActiveNotExpired(@Param("status") String status, @Param("now") LocalDateTime now);

    @Query("select i from UserBotInvestment i join fetch i.user join fetch i.bot "
            + "where i.status = :status and i.expiresAt <= :now")
    List<UserBotInvestment> findActiveExpired(@Param("status") String status, @Param("now") LocalDateTime now);

    Optional<UserBotInvestment> findByUserIdAndBotIdAndStatus(Long userId, Long botId, String status);

    long countByBotId(Long botId);

    long countByBotIdAndStatus(Long botId, String status);

    long countByStatus(String status);

    @Query("select count(distinct i.user.id) from UserBotInvestment i where i.bot.id = :botId")
    long countDistinctUsersByBotId(@Param("botId") Long botId);

    @Query("select coalesce(sum(i.investmentAmount), 0) from UserBotInvestment i")
    BigDecimal sumInvestmentAmount();

    @Query("select coalesce(sum(i.totalProfit), 0) from UserBotInvestment i")
    BigDecimal sumTotalProfit();

    @Query("select coalesce(sum(i.investmentAmount), 0) from UserBotInvestment i where i.bot.id = :botId")
    BigDecimal sumInvestmentAmountByBotId(@Param("botId") Long botId);

    @Query("select coalesce(sum(i.totalProfit), 0) from UserBotInvestment i where i.bot.id = :botId")
    BigDecimal sumTotalProfitByBotId(@Param("botId") Long botId);

    @Query("select coalesce(avg(i.successfulTrades), 0) from UserBotInvestment i where i.bot.id = :botId")
    Double avgSuccessfulTradesByBotId(@Param("botId") Long botId);

    @Query("select coalesce(avg(i.failedTrades), 0) from UserBotInvestment i where i.bot.id = :botId")
    Double avgFailedTradesByBotId(@Param("botId") Long botId);
}
