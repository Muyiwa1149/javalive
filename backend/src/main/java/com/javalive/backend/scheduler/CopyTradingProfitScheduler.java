package com.javalive.backend.scheduler;

import com.javalive.backend.entity.AppSetting;
import com.javalive.backend.entity.CopyTradingExpert;
import com.javalive.backend.entity.LedgerTransaction;
import com.javalive.backend.entity.User;
import com.javalive.backend.entity.UserCopyTrade;
import com.javalive.backend.repository.LedgerTransactionRepository;
import com.javalive.backend.repository.UserCopyTradeRepository;
import com.javalive.backend.service.mail.MailService;
import com.javalive.backend.service.settings.SettingsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Java port of the source app's canonical copy-trading profit simulation — {@code CopyTradingController::
 * automaticCopyTradingProfits()} (the user-side controller kept per the "new system" decision in
 * {@code docs/CANONICAL-MODULES.md}), NOT the version the source's {@code GenerateCopyTradingProfits} command
 * or {@code AutoTaskController} actually wire up (both of those target the retired legacy copy-trading
 * pair and would be inert against this schema). This one's win/loss math (expert {@code win_rate}-driven,
 * escrowed {@code current_balance} per position) is what Phase 4/5's start/stop flows already assume.
 */
@Component
public class CopyTradingProfitScheduler {

    private static final Logger log = LoggerFactory.getLogger(CopyTradingProfitScheduler.class);

    private final UserCopyTradeRepository userCopyTradeRepository;
    private final LedgerTransactionRepository ledgerTransactionRepository;
    private final SettingsService settingsService;
    private final MailService mailService;

    public CopyTradingProfitScheduler(UserCopyTradeRepository userCopyTradeRepository,
                                       LedgerTransactionRepository ledgerTransactionRepository,
                                       SettingsService settingsService,
                                       MailService mailService) {
        this.userCopyTradeRepository = userCopyTradeRepository;
        this.ledgerTransactionRepository = ledgerTransactionRepository;
        this.settingsService = settingsService;
        this.mailService = mailService;
    }

    /** {@code ->everyThirtyMinutes()} in Kernel.php. */
    @Scheduled(initialDelay = 45_000, fixedDelay = 1_800_000)
    @Transactional
    public void generateProfits() {
        AppSetting settings = settingsService.get();
        if (settings == null || !"on".equals(settings.getTradeMode())) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        List<UserCopyTrade> activeTrades = userCopyTradeRepository.findByActiveWithUserAndExpertOrderByCreatedAtDesc("yes");
        int processed = 0;

        for (UserCopyTrade copyTrade : activeTrades) {
            try {
                if (generateOneProfit(copyTrade, now)) {
                    processed++;
                }
            } catch (Exception e) {
                log.error("Failed to generate profit for copy trade #{}: {}", copyTrade.getId(), e.getMessage());
            }
        }

        if (processed > 0) {
            log.info("Copy trading profits generated for {} positions.", processed);
        }
    }

    private boolean generateOneProfit(UserCopyTrade copyTrade, LocalDateTime now) {
        LocalDateTime lastProfit = copyTrade.getLastProfitAt() != null ? copyTrade.getLastProfitAt() : copyTrade.getStartedAt();
        if (lastProfit == null) {
            lastProfit = copyTrade.getCreatedAt();
        }

        ThreadLocalRandom random = ThreadLocalRandom.current();
        int hoursToAdd = random.nextInt(2, 9);
        LocalDateTime nextProfitTime = lastProfit.plusHours(hoursToAdd);
        if (now.isBefore(nextProfitTime)) {
            return false;
        }

        CopyTradingExpert expert = copyTrade.getExpert();
        boolean isProfit = random.nextInt(1, 101) <= expert.getWinRate();
        BigDecimal currentBalance = copyTrade.getCurrentBalance();

        BigDecimal amount;
        BigDecimal newBalance;
        BigDecimal totalProfit = copyTrade.getTotalProfit();
        int winningTrades = copyTrade.getWinningTrades();

        if (isProfit) {
            BigDecimal profitPct = BigDecimal.valueOf(random.nextInt(50, 401)).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
            amount = currentBalance.multiply(profitPct).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            newBalance = currentBalance.add(amount);
            totalProfit = totalProfit.add(amount);
            winningTrades++;
        } else {
            BigDecimal lossPct = BigDecimal.valueOf(random.nextInt(20, 201)).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
            amount = currentBalance.multiply(lossPct).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            newBalance = currentBalance.subtract(amount).max(BigDecimal.ZERO);
            totalProfit = totalProfit.subtract(amount);
        }

        int totalTrades = copyTrade.getTotalTrades() + 1;
        BigDecimal price = copyTrade.getPrice() != null && copyTrade.getPrice().signum() != 0 ? copyTrade.getPrice() : BigDecimal.ONE;
        BigDecimal profitPercentage = newBalance.subtract(price).divide(price, 6, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));

        copyTrade.setCurrentBalance(newBalance);
        copyTrade.setTotalProfit(totalProfit);
        copyTrade.setTotalTrades(totalTrades);
        copyTrade.setWinningTrades(winningTrades);
        copyTrade.setProfitPercentage(profitPercentage);
        copyTrade.setLastProfitAt(now);
        copyTrade.setUpdatedAt(now);
        userCopyTradeRepository.save(copyTrade);

        User user = copyTrade.getUser();
        ledgerTransactionRepository.save(LedgerTransaction.builder()
                .user(user)
                .planLabel("Copy Trading - " + expert.getName())
                .amount(amount)
                .type(isProfit ? "Copy Trading Profit" : "Copy Trading Loss")
                .createdAt(now)
                .updatedAt(now)
                .build());

        if (Boolean.TRUE.equals(user.getSendRoiEmail()) && isProfit
                && amount.compareTo(copyTrade.getPrice().multiply(BigDecimal.valueOf(0.05))) > 0) {
            String message = String.format("Great news! Your copy trading with %s generated a profit of %s%s. Current balance: %s%s",
                    expert.getName(), nullToEmpty(user.getCurrencySymbol()), amount.toPlainString(),
                    nullToEmpty(user.getCurrencySymbol()), newBalance.setScale(2, RoundingMode.HALF_UP).toPlainString());
            mailService.send(user.getEmail(), "Copy Trading Profit - " + expert.getName(), message);
        }

        return true;
    }

    private String nullToEmpty(String value) {
        return value != null ? value : "";
    }
}
