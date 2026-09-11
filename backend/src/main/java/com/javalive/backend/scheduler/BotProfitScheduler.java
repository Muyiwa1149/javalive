package com.javalive.backend.scheduler;

import com.javalive.backend.entity.AppSetting;
import com.javalive.backend.entity.BotTradingHistory;
import com.javalive.backend.entity.LedgerTransaction;
import com.javalive.backend.entity.TradingBot;
import com.javalive.backend.entity.User;
import com.javalive.backend.entity.UserBotInvestment;
import com.javalive.backend.repository.LedgerTransactionRepository;
import com.javalive.backend.repository.BotTradingHistoryRepository;
import com.javalive.backend.repository.TradingBotRepository;
import com.javalive.backend.repository.UserBotInvestmentRepository;
import com.javalive.backend.repository.UserRepository;
import com.javalive.backend.service.mail.MailService;
import com.javalive.backend.service.notification.NotificationService;
import com.javalive.backend.service.settings.SettingsService;
import com.javalive.backend.util.MoneyFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Java port of the source app's {@code AutoTaskController::automaticBotTradingProfits()} — the real
 * live bot-profit engine behind the "AI Bots" feature (one of the six nav-hidden-but-fully-built
 * modules the user chose to make live, per {@code docs/CANONICAL-MODULES.md}). Source never actually
 * scheduled this in {@code Kernel.php} at all; it was only reachable via an unauthenticated
 * {@code GET /cron} route (bundling ROI + trading + copy-trading + bot profit in one call) or the
 * manual {@code bot:bulk-trades} CLI backfill tool. Neither is real automation — ported here as a
 * proper {@code @Scheduled} job so the Bots feature Phase 4/5 already built is actually functional,
 * consistent with how ROI and copy-trading profits were consolidated.
 */
@Component
public class BotProfitScheduler {

    private static final Logger log = LoggerFactory.getLogger(BotProfitScheduler.class);

    private static final String[] TRADING_PAIRS_FALLBACK = {"EUR/USD", "GBP/USD", "BTC/USD"};
    private static final String[] STRATEGIES = {
            "Trend Following", "Scalping", "Momentum Trading", "Mean Reversion", "Breakout Strategy",
            "Support & Resistance", "RSI Divergence", "MACD Crossover", "Moving Average Strategy", "Fibonacci Retracement"
    };

    /** Cosmetic reference prices for trade-history display only — same table as source's {@code getBasePriceForPair()}. */
    private static final Map<String, BigDecimal> BASE_PRICES = Map.ofEntries(
            Map.entry("EUR/USD", BigDecimal.valueOf(1.0850)), Map.entry("GBP/USD", BigDecimal.valueOf(1.2650)),
            Map.entry("USD/JPY", BigDecimal.valueOf(149.50)), Map.entry("USD/CHF", BigDecimal.valueOf(0.8950)),
            Map.entry("AUD/USD", BigDecimal.valueOf(0.6750)), Map.entry("USD/CAD", BigDecimal.valueOf(1.3450)),
            Map.entry("BTC/USD", BigDecimal.valueOf(43500.00)), Map.entry("ETH/USD", BigDecimal.valueOf(2650.00)),
            Map.entry("BNB/USD", BigDecimal.valueOf(315.00)), Map.entry("ADA/USD", BigDecimal.valueOf(0.485)),
            Map.entry("SOL/USD", BigDecimal.valueOf(98.50)), Map.entry("DOT/USD", BigDecimal.valueOf(7.25))
    );

    private final UserBotInvestmentRepository investmentRepository;
    private final BotTradingHistoryRepository historyRepository;
    private final TradingBotRepository botRepository;
    private final UserRepository userRepository;
    private final LedgerTransactionRepository ledgerTransactionRepository;
    private final NotificationService notificationService;
    private final SettingsService settingsService;
    private final MailService mailService;

    public BotProfitScheduler(UserBotInvestmentRepository investmentRepository,
                               BotTradingHistoryRepository historyRepository,
                               TradingBotRepository botRepository,
                               UserRepository userRepository,
                               LedgerTransactionRepository ledgerTransactionRepository,
                               NotificationService notificationService,
                               SettingsService settingsService,
                               MailService mailService) {
        this.investmentRepository = investmentRepository;
        this.historyRepository = historyRepository;
        this.botRepository = botRepository;
        this.userRepository = userRepository;
        this.ledgerTransactionRepository = ledgerTransactionRepository;
        this.notificationService = notificationService;
        this.settingsService = settingsService;
        this.mailService = mailService;
    }

    @Scheduled(initialDelay = 60_000, fixedDelay = 1_800_000)
    @Transactional
    public void processBotTrading() {
        AppSetting settings = settingsService.get();
        if (settings == null || !"on".equals(settings.getTradeMode())) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        List<UserBotInvestment> active = investmentRepository.findActiveNotExpired("active", now);
        int processed = 0;
        for (UserBotInvestment investment : active) {
            try {
                if (generateOneTrade(investment, now)) {
                    processed++;
                }
            } catch (Exception e) {
                log.error("Failed to generate bot trade for investment #{}: {}", investment.getId(), e.getMessage());
            }
        }

        List<UserBotInvestment> expired = investmentRepository.findActiveExpired("active", now);
        int completed = 0;
        for (UserBotInvestment investment : expired) {
            try {
                completeInvestment(investment, now);
                completed++;
            } catch (Exception e) {
                log.error("Failed to complete bot investment #{}: {}", investment.getId(), e.getMessage());
            }
        }

        if (processed > 0 || completed > 0) {
            log.info("Bot trading processed for {} investments. {} investments completed.", processed, completed);
        }
    }

    private boolean generateOneTrade(UserBotInvestment investment, LocalDateTime now) {
        LocalDateTime lastProfit = investment.getLastProfitAt() != null ? investment.getLastProfitAt() : investment.getStartedAt();
        if (lastProfit == null) {
            lastProfit = investment.getCreatedAt();
        }

        ThreadLocalRandom random = ThreadLocalRandom.current();
        int hoursToAdd = random.nextInt(4, 9);
        if (now.isBefore(lastProfit.plusHours(hoursToAdd))) {
            return false;
        }

        TradingBot bot = investment.getBot();
        User user = investment.getUser();
        BigDecimal currentBalance = investment.getCurrentBalance();

        int successRate = bot.getSuccessRate() != null ? bot.getSuccessRate() : 80;
        boolean isProfit = random.nextInt(1, 101) <= successRate;

        String tradingPair = TRADING_PAIRS_FALLBACK[random.nextInt(TRADING_PAIRS_FALLBACK.length)];
        BigDecimal basePrice = BASE_PRICES.getOrDefault(tradingPair, BigDecimal.ONE);
        BigDecimal entryPrice = basePrice.multiply(BigDecimal.ONE.add(
                BigDecimal.valueOf(random.nextInt(-100, 101)).divide(BigDecimal.valueOf(10000), 8, RoundingMode.HALF_UP)));
        String tradeType = random.nextBoolean() ? "BUY" : "SELL";
        String strategy = STRATEGIES[random.nextInt(STRATEGIES.length)];

        BigDecimal amount = currentBalance.multiply(BigDecimal.valueOf(0.1));

        if (isProfit) {
            BigDecimal min = bot.getDailyProfitMin() != null ? bot.getDailyProfitMin() : BigDecimal.valueOf(0.5);
            BigDecimal max = bot.getDailyProfitMax() != null ? bot.getDailyProfitMax() : BigDecimal.valueOf(2);
            int minCents = min.multiply(BigDecimal.valueOf(100)).intValue();
            int maxCents = Math.max(minCents + 1, max.multiply(BigDecimal.valueOf(100)).intValue() + 1);
            BigDecimal profitPct = BigDecimal.valueOf(random.nextInt(minCents, maxCents)).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
            BigDecimal profit = currentBalance.multiply(profitPct).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            BigDecimal exitPrice = "BUY".equals(tradeType)
                    ? entryPrice.multiply(BigDecimal.ONE.add(profitPct.divide(BigDecimal.valueOf(100), 8, RoundingMode.HALF_UP)))
                    : entryPrice.multiply(BigDecimal.ONE.subtract(profitPct.divide(BigDecimal.valueOf(100), 8, RoundingMode.HALF_UP)));

            investment.setCurrentBalance(currentBalance.add(profit));
            investment.setTotalProfit(investment.getTotalProfit().add(profit));
            investment.setSuccessfulTrades(investment.getSuccessfulTrades() + 1);
            investment.setLastProfitAt(now);
            investment.setUpdatedAt(now);
            investmentRepository.save(investment);

            historyRepository.save(BotTradingHistory.builder()
                    .userBotInvestment(investment).tradeType(tradeType).tradingPair(tradingPair)
                    .entryPrice(entryPrice.setScale(5, RoundingMode.HALF_UP)).exitPrice(exitPrice.setScale(5, RoundingMode.HALF_UP))
                    .amount(amount).profitLoss(profit).profitPercentage(profitPct.setScale(2, RoundingMode.HALF_UP))
                    .result("profit").strategyUsed(strategy)
                    .openedAt(now.minusMinutes(random.nextInt(15, 181))).closedAt(now)
                    .createdAt(now).updatedAt(now).build());

            ledgerTransactionRepository.save(LedgerTransaction.builder()
                    .user(user).planLabel("Bot Trading Profit - " + bot.getName()).amount(profit)
                    .type("Bot Trading Profit").createdAt(now).updatedAt(now).build());

            bot.setLastTradeAt(now);
            bot.setTotalEarned(bot.getTotalEarned().add(profit));
            botRepository.save(bot);

            notificationService.notifyUser(user, "Bot Trading Profit",
                    "Your " + bot.getName() + " trading bot has generated a profit of " + nullToEmpty(user.getCurrencySymbol())
                            + profit.toPlainString() + " using " + strategy + " strategy on " + tradingPair + ".",
                    "success", investment.getId(), "UserBotInvestment");

            if (Boolean.TRUE.equals(user.getSendRoiEmail())) {
                mailService.send(user.getEmail(), "Bot Trading Profit Earned",
                        "Your " + bot.getName() + " trading bot has generated a profit of " + nullToEmpty(user.getCurrencySymbol())
                                + profit.toPlainString() + ". Keep investing and earning!");
            }
        } else {
            BigDecimal lossPct = BigDecimal.valueOf(random.nextInt(50, 201)).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
            BigDecimal loss = currentBalance.multiply(lossPct).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            BigDecimal exitPrice = "BUY".equals(tradeType)
                    ? entryPrice.multiply(BigDecimal.ONE.subtract(lossPct.divide(BigDecimal.valueOf(100), 8, RoundingMode.HALF_UP)))
                    : entryPrice.multiply(BigDecimal.ONE.add(lossPct.divide(BigDecimal.valueOf(100), 8, RoundingMode.HALF_UP)));

            investment.setCurrentBalance(currentBalance.subtract(loss).max(BigDecimal.ZERO));
            investment.setTotalLoss(investment.getTotalLoss().add(loss));
            investment.setFailedTrades(investment.getFailedTrades() + 1);
            investment.setLastProfitAt(now);
            investment.setUpdatedAt(now);
            investmentRepository.save(investment);

            historyRepository.save(BotTradingHistory.builder()
                    .userBotInvestment(investment).tradeType(tradeType).tradingPair(tradingPair)
                    .entryPrice(entryPrice.setScale(5, RoundingMode.HALF_UP)).exitPrice(exitPrice.setScale(5, RoundingMode.HALF_UP))
                    .amount(amount).profitLoss(loss.negate()).profitPercentage(lossPct.negate().setScale(2, RoundingMode.HALF_UP))
                    .result("loss").strategyUsed(strategy)
                    .openedAt(now.minusMinutes(random.nextInt(15, 181))).closedAt(now)
                    .createdAt(now).updatedAt(now).build());

            bot.setLastTradeAt(now);
            botRepository.save(bot);

            if (lossPct.compareTo(BigDecimal.ONE) > 0) {
                notificationService.notifyUser(user, "Bot Trading Alert",
                        "Your " + bot.getName() + " bot had a trade loss of " + lossPct + "% on " + tradingPair
                                + ". The system has automatically adjusted the strategy.",
                        "warning", investment.getId(), "UserBotInvestment");
            }
        }

        return true;
    }

    private void completeInvestment(UserBotInvestment investment, LocalDateTime now) {
        User user = investment.getUser();
        TradingBot bot = investment.getBot();
        BigDecimal totalReturn = investment.getCurrentBalance();

        if (totalReturn.signum() > 0) {
            user.setAccountBalance(user.getAccountBalance().add(totalReturn));
            user.setUpdatedAt(now);
            userRepository.save(user);

            ledgerTransactionRepository.save(LedgerTransaction.builder()
                    .user(user).planLabel("Bot Investment Completed - " + bot.getName()).amount(totalReturn)
                    .type("Bot Investment Return").createdAt(now).updatedAt(now).build());
        }

        investment.setStatus("completed");
        investment.setUpdatedAt(now);
        investmentRepository.save(investment);

        BigDecimal netProfit = investment.getTotalProfit().subtract(investment.getTotalLoss());
        String message = netProfit.signum() > 0
                ? "Your " + bot.getName() + " bot investment has completed with a profit of " + nullToEmpty(user.getCurrencySymbol())
                        + MoneyFormat.of(netProfit) + ". The funds have been credited to your account balance."
                : "Your " + bot.getName() + " bot investment has completed with a final balance of "
                        + nullToEmpty(user.getCurrencySymbol()) + MoneyFormat.of(totalReturn) + ". The funds have been credited to your account balance.";

        notificationService.notifyUser(user, "Bot Investment Completed", message,
                netProfit.signum() > 0 ? "success" : "info", investment.getId(), "UserBotInvestment");

        if (Boolean.TRUE.equals(user.getSendRoiEmail())) {
            mailService.send(user.getEmail(), "Bot Investment Completed",
                    "Your " + bot.getName() + " bot investment has completed. Total return: "
                            + nullToEmpty(user.getCurrencySymbol()) + totalReturn.toPlainString() + ", Net profit: "
                            + nullToEmpty(user.getCurrencySymbol()) + netProfit.toPlainString());
        }
    }

    private String nullToEmpty(String value) {
        return value != null ? value : "";
    }
}
