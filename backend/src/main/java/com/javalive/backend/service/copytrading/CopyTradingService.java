package com.javalive.backend.service.copytrading;

import com.javalive.backend.dto.copytrading.CopyTradeSummary;
import com.javalive.backend.dto.copytrading.CopyTradingDashboard;
import com.javalive.backend.dto.copytrading.ExpertSummary;
import com.javalive.backend.dto.copytrading.SimulatedTrade;
import com.javalive.backend.dto.copytrading.StartCopyTradingRequest;
import com.javalive.backend.entity.CopyTradingExpert;
import com.javalive.backend.entity.LedgerTransaction;
import com.javalive.backend.entity.User;
import com.javalive.backend.entity.UserCopyTrade;
import com.javalive.backend.repository.CopyTradingExpertRepository;
import com.javalive.backend.repository.LedgerTransactionRepository;
import com.javalive.backend.repository.UserCopyTradeRepository;
import com.javalive.backend.repository.UserRepository;
import com.javalive.backend.service.mail.MailService;
import com.javalive.backend.web.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Canonical copy-trading pair per docs/CANONICAL-MODULES.md (CopyTradingController /
 * CopyTradingAdminController — the "new system", cleaner validation, blocks unsafe deletes).
 * The actual periodic profit/loss simulation (source's automaticCopyTradingProfits, scheduled
 * every few hours) is deliberately NOT built here — that's consolidated scheduled-job work
 * tracked for Phase 6, same as ROI and bot profit jobs.
 */
@Service
public class CopyTradingService {

    private static final Set<String> PAIRS = Set.of("EUR/USD", "GBP/USD", "USD/JPY", "AUD/USD", "USD/CHF");

    private final CopyTradingExpertRepository expertRepository;
    private final UserCopyTradeRepository userCopyTradeRepository;
    private final UserRepository userRepository;
    private final LedgerTransactionRepository ledgerTransactionRepository;
    private final MailService mailService;

    public CopyTradingService(CopyTradingExpertRepository expertRepository, UserCopyTradeRepository userCopyTradeRepository,
                               UserRepository userRepository, LedgerTransactionRepository ledgerTransactionRepository,
                               MailService mailService) {
        this.expertRepository = expertRepository;
        this.userCopyTradeRepository = userCopyTradeRepository;
        this.userRepository = userRepository;
        this.ledgerTransactionRepository = ledgerTransactionRepository;
        this.mailService = mailService;
    }

    @Transactional(readOnly = true)
    public CopyTradingDashboard dashboard(Long userId) {
        List<UserCopyTrade> all = userCopyTradeRepository.findByUserIdOrderByCreatedAtDesc(userId);
        List<UserCopyTrade> active = all.stream().filter(c -> "yes".equals(c.getActive())).toList();

        BigDecimal totalInvested = active.stream().map(UserCopyTrade::getPrice).filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal currentBalance = active.stream().map(UserCopyTrade::getCurrentBalance).filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalProfit = active.stream().map(UserCopyTrade::getTotalProfit).filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CopyTradingDashboard(all.stream().map(CopyTradeSummary::from).toList(),
                active.size(), totalInvested, currentBalance, totalProfit);
    }

    @Transactional(readOnly = true)
    public List<ExpertSummary> experts(Long userId) {
        List<CopyTradingExpert> experts = expertRepository.findByStatusOrderByRatingDescWinRateDesc("active");
        Set<Long> copyingIds = userId == null ? Set.of() : userCopyTradeRepository.findByUserIdAndActive(userId, "yes")
                .stream().map(c -> c.getExpert().getId()).collect(java.util.stream.Collectors.toSet());
        return experts.stream().map(e -> ExpertSummary.from(e, copyingIds.contains(e.getId()))).toList();
    }

    @Transactional
    public CopyTradeSummary start(Long userId, StartCopyTradingRequest request) {
        User user = findUser(userId);
        CopyTradingExpert expert = expertRepository.findById(request.expertId())
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Expert trader not available."));
        if (!"active".equals(expert.getStatus())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Expert trader not available.");
        }
        if (expert.getPrice() != null && request.amount().compareTo(expert.getPrice()) < 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Minimum investment for " + expert.getName() + " is " + user.getCurrencySymbol() + expert.getPrice() + ".");
        }
        if (user.getAccountBalance().compareTo(request.amount()) < 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Insufficient account balance. Please fund your account first.");
        }
        if (userCopyTradeRepository.findByUserIdAndExpertIdAndActive(userId, expert.getId(), "yes").isPresent()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "You are already copying " + expert.getName() + ".");
        }

        LocalDateTime now = LocalDateTime.now();
        user.setAccountBalance(user.getAccountBalance().subtract(request.amount()));
        user.setUpdatedAt(now);
        userRepository.save(user);

        UserCopyTrade copyTrade = UserCopyTrade.builder()
                .user(user).expert(expert).price(request.amount()).currentBalance(request.amount())
                .active("yes").type(expert.getType() != null ? expert.getType() : "expert")
                .startedAt(now).lastProfitAt(now).totalProfit(BigDecimal.ZERO).totalTrades(0).winningTrades(0)
                .profitPercentage(BigDecimal.ZERO).createdAt(now).updatedAt(now)
                .build();
        copyTrade = userCopyTradeRepository.save(copyTrade);

        ledgerTransactionRepository.save(LedgerTransaction.builder()
                .user(user).planLabel("Copy Trading - " + expert.getName()).amount(request.amount())
                .type("Copy Trading Investment").createdAt(now).updatedAt(now).build());

        expert.setFollowers(expert.getFollowers() + 1);
        expertRepository.save(expert);

        if (Boolean.TRUE.equals(user.getSendRoiEmail())) {
            mailService.send(user.getEmail(), "Copy Trading Started - " + expert.getName(),
                    "You have successfully started copying " + expert.getName() + " with an investment of "
                            + user.getCurrencySymbol() + request.amount() + ". You'll receive profits based on the expert's trading performance.");
        }

        return CopyTradeSummary.from(copyTrade);
    }

    @Transactional
    public void stop(Long userId, Long copyTradeId) {
        UserCopyTrade copyTrade = userCopyTradeRepository.findById(copyTradeId)
                .filter(c -> c.getUser().getId().equals(userId) && "yes".equals(c.getActive()))
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Copy trade not found or already stopped."));

        User user = copyTrade.getUser();
        BigDecimal totalReturn = copyTrade.getCurrentBalance();

        copyTrade.setActive("no");
        copyTrade.setUpdatedAt(LocalDateTime.now());
        userCopyTradeRepository.save(copyTrade);

        user.setAccountBalance(user.getAccountBalance().add(totalReturn));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        ledgerTransactionRepository.save(LedgerTransaction.builder()
                .user(user).planLabel("Copy Trading Stopped - " + copyTrade.getExpert().getName()).amount(totalReturn)
                .type("Copy Trading Return").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build());

        CopyTradingExpert expert = copyTrade.getExpert();
        if (expert.getFollowers() > 0) {
            expert.setFollowers(expert.getFollowers() - 1);
            expertRepository.save(expert);
        }

        if (Boolean.TRUE.equals(user.getSendRoiEmail())) {
            mailService.send(user.getEmail(), "Copy Trading Stopped - " + copyTrade.getExpert().getName(),
                    "You have stopped copying " + copyTrade.getExpert().getName() + ". Your total return of "
                            + user.getCurrencySymbol() + totalReturn + " (including " + user.getCurrencySymbol()
                            + copyTrade.getTotalProfit() + " profit) has been credited to your account.");
        }
    }

    @Transactional(readOnly = true)
    public List<SimulatedTrade> recentActivity(Long userId, Long copyTradeId) {
        findOwnedCopyTrade(userId, copyTradeId);
        ThreadLocalRandom random = ThreadLocalRandom.current();
        String[] pairs = PAIRS.toArray(new String[0]);
        return java.util.stream.IntStream.range(0, 10).mapToObj(i -> {
            boolean isProfit = random.nextInt(1, 101) <= 70;
            BigDecimal amount = BigDecimal.valueOf(isProfit ? random.nextInt(5, 51) : -random.nextInt(5, 31));
            return new SimulatedTrade(i + 1, pairs[random.nextInt(pairs.length)], amount,
                    LocalDateTime.now().minusDays(random.nextInt(0, 31)));
        }).sorted((a, b) -> b.createdAt().compareTo(a.createdAt())).toList();
    }

    @Transactional(readOnly = true)
    public CopyTradeSummary analytics(Long userId, Long copyTradeId) {
        UserCopyTrade copyTrade = findOwnedCopyTrade(userId, copyTradeId);
        return CopyTradeSummary.from(copyTrade);
    }

    public long daysActive(CopyTradeSummary copyTrade) {
        if (copyTrade.startedAt() == null) return 0;
        return Duration.between(copyTrade.startedAt(), LocalDateTime.now()).toDays();
    }

    private UserCopyTrade findOwnedCopyTrade(Long userId, Long copyTradeId) {
        return userCopyTradeRepository.findById(copyTradeId)
                .filter(c -> c.getUser().getId().equals(userId))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Copy trade not found."));
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found."));
    }
}
