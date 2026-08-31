package com.javalive.backend.repository;

import com.javalive.backend.entity.BotTradingHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface BotTradingHistoryRepository extends JpaRepository<BotTradingHistory, Long> {

    List<BotTradingHistory> findByUserBotInvestmentIdOrderByOpenedAtDesc(Long userBotInvestmentId);

    List<BotTradingHistory> findByUserBotInvestmentUserIdOrderByOpenedAtDesc(Long userId);

    long countByUserBotInvestment_Bot_Id(Long botId);

    long countByUserBotInvestment_Bot_IdAndResult(Long botId, String result);

    List<BotTradingHistory> findByUserBotInvestment_Bot_IdOrderByOpenedAtDesc(Long botId);

    /** Day-bucketed profit sum, single grouped query instead of source's per-day query-in-a-loop. */
    @Query(value = "select date(t.closed_at) as d, coalesce(sum(t.profit_loss), 0) as total "
            + "from bot_trading_history t inner join user_bot_investments i on i.id = t.user_bot_investment_id "
            + "where i.bot_id = :botId and t.closed_at >= :since group by date(t.closed_at)", nativeQuery = true)
    List<Object[]> dailyProfitByBotId(@Param("botId") Long botId, @Param("since") LocalDateTime since);

    @Query(value = "select date(t.closed_at) as d, coalesce(sum(t.profit_loss), 0) as total "
            + "from bot_trading_history t where t.closed_at >= :since group by date(t.closed_at)", nativeQuery = true)
    List<Object[]> dailyProfitAll(@Param("since") LocalDateTime since);
}
