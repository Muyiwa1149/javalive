package com.javalive.backend.service.bot;

import com.javalive.backend.dto.bot.BotDashboard;
import com.javalive.backend.dto.bot.BotDetail;
import com.javalive.backend.dto.bot.BotInvestmentSummary;
import com.javalive.backend.dto.bot.BotSummary;
import com.javalive.backend.dto.bot.BotTradeSummary;
import com.javalive.backend.dto.bot.InvestBotRequest;
import com.javalive.backend.entity.LedgerTransaction;
import com.javalive.backend.entity.TradingBot;
import com.javalive.backend.entity.User;
import com.javalive.backend.entity.UserBotInvestment;
import com.javalive.backend.repository.BotTradingHistoryRepository;
import com.javalive.backend.repository.LedgerTransactionRepository;
import com.javalive.backend.repository.TradingBotRepository;
import com.javalive.backend.repository.UserBotInvestmentRepository;
import com.javalive.backend.repository.UserRepository;
import com.javalive.backend.service.mail.MailService;
import com.javalive.backend.util.MoneyFormat;
import com.javalive.backend.web.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/** Mirrors the source app's UserBotController — AI trading bot investments, canonical per checklist ("now live in nav"). */
@Service
public class BotService {

    private final TradingBotRepository botRepository;
    private final UserBotInvestmentRepository investmentRepository;
    private final BotTradingHistoryRepository tradingHistoryRepository;
    private final UserRepository userRepository;
    private final LedgerTransactionRepository ledgerTransactionRepository;
    private final MailService mailService;

    public BotService(TradingBotRepository botRepository, UserBotInvestmentRepository investmentRepository,
                       BotTradingHistoryRepository tradingHistoryRepository, UserRepository userRepository,
                       LedgerTransactionRepository ledgerTransactionRepository, MailService mailService) {
        this.botRepository = botRepository;
        this.investmentRepository = investmentRepository;
        this.tradingHistoryRepository = tradingHistoryRepository;
        this.userRepository = userRepository;
        this.ledgerTransactionRepository = ledgerTransactionRepository;
        this.mailService = mailService;
    }

    @Transactional(readOnly = true)
    public List<BotSummary> listBots(Long userId) {
        List<TradingBot> bots = botRepository.findByStatus("active");
        return bots.stream().map(b -> BotSummary.from(b, hasActiveInvestment(userId, b.getId()))).toList();
    }

    @Transactional(readOnly = true)
    public BotDetail botDetail(Long userId, Long botId) {
        TradingBot bot = botRepository.findById(botId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Bot not found."));

        BotInvestmentSummary myInvestment = investmentRepository.findByUserIdAndBotIdAndStatus(userId, botId, "active")
                .map(BotInvestmentSummary::from).orElse(null);

        List<BotTradeSummary> recentTrades = myInvestment != null
                ? tradingHistoryRepository.findByUserBotInvestmentIdOrderByOpenedAtDesc(myInvestment.id())
                        .stream().map(BotTradeSummary::from).toList()
                : List.of();

        BigDecimal expectedDailyReturn = bot.getDailyProfitMin().add(bot.getDailyProfitMax())
                .divide(BigDecimal.valueOf(2), 4, java.math.RoundingMode.HALF_UP);

        return new BotDetail(BotSummary.from(bot, myInvestment != null), myInvestment, recentTrades,
                expectedDailyReturn, 0, bot.getTotalUsers());
    }

    @Transactional
    public BotInvestmentSummary invest(Long userId, Long botId, InvestBotRequest request) {
        User user = findUser(userId);
        TradingBot bot = botRepository.findById(botId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Bot not found."));

        if (!"active".equals(bot.getStatus())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "This bot is currently not available for investment.");
        }
        if (request.amount().compareTo(bot.getMinInvestment()) < 0 || request.amount().compareTo(bot.getMaxInvestment()) > 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Investment amount must be between " + user.getCurrencySymbol() + MoneyFormat.of(bot.getMinInvestment())
                            + " and " + user.getCurrencySymbol() + MoneyFormat.of(bot.getMaxInvestment()) + ".");
        }
        if (user.getAccountBalance().compareTo(request.amount()) < 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Insufficient account balance. Please fund your account first.");
        }
        if (hasActiveInvestment(userId, botId)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "You already have an active investment with this bot.");
        }

        LocalDateTime now = LocalDateTime.now();
        user.setAccountBalance(user.getAccountBalance().subtract(request.amount()));
        user.setUpdatedAt(now);
        userRepository.save(user);

        UserBotInvestment investment = UserBotInvestment.builder()
                .user(user).bot(bot).investmentAmount(request.amount()).currentBalance(request.amount())
                .totalProfit(BigDecimal.ZERO).totalLoss(BigDecimal.ZERO).successfulTrades(0).failedTrades(0)
                .startedAt(now).expiresAt(now.plusDays(bot.getDurationDays())).lastProfitAt(now)
                .status("active").autoReinvest(request.autoReinvest())
                .reinvestPercentage(request.reinvestPercentage() != null ? request.reinvestPercentage() : BigDecimal.ZERO)
                .createdAt(now).updatedAt(now)
                .build();
        investment = investmentRepository.save(investment);

        ledgerTransactionRepository.save(LedgerTransaction.builder()
                .user(user).planLabel("Bot Investment - " + bot.getName()).amount(request.amount())
                .type("Bot Investment").createdAt(now).updatedAt(now).build());

        bot.setTotalUsers(bot.getTotalUsers() + 1);
        botRepository.save(bot);

        if (Boolean.TRUE.equals(user.getSendRoiEmail())) {
            mailService.send(user.getEmail(), "Bot Investment Confirmed - " + bot.getName(),
                    "You have successfully invested " + user.getCurrencySymbol() + MoneyFormat.of(request.amount()) + " in the " + bot.getName()
                            + " trading bot. Your investment will be active for " + bot.getDurationDays() + " days.");
        }

        return BotInvestmentSummary.from(investment);
    }

    @Transactional
    public void cancel(Long userId, Long investmentId) {
        UserBotInvestment investment = investmentRepository.findById(investmentId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Investment not found."));
        if (!investment.getUser().getId().equals(userId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Unauthorized action.");
        }
        if (!"active".equals(investment.getStatus())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "This investment is not active.");
        }

        User user = investment.getUser();
        BigDecimal refund = investment.getCurrentBalance();

        user.setAccountBalance(user.getAccountBalance().add(refund));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        investment.setStatus("cancelled");
        investment.setUpdatedAt(LocalDateTime.now());
        investmentRepository.save(investment);

        ledgerTransactionRepository.save(LedgerTransaction.builder()
                .user(user).planLabel("Bot Investment Cancelled - " + investment.getBot().getName()).amount(refund)
                .type("Bot Investment Refund").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build());

        if (Boolean.TRUE.equals(user.getSendRoiEmail())) {
            mailService.send(user.getEmail(), "Bot Investment Cancelled",
                    "Your investment in " + investment.getBot().getName() + " has been cancelled and "
                            + refund + " has been refunded to your account.");
        }
    }

    @Transactional(readOnly = true)
    public BotDashboard dashboard(Long userId) {
        List<UserBotInvestment> all = investmentRepository.findByUserIdOrderByCreatedAtDesc(userId);
        List<UserBotInvestment> active = all.stream().filter(i -> "active".equals(i.getStatus())).toList();

        BigDecimal totalInvested = all.stream().map(UserBotInvestment::getInvestmentAmount).filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal currentBalance = active.stream().map(UserBotInvestment::getCurrentBalance).filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalProfit = all.stream().map(UserBotInvestment::getTotalProfit).filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalLoss = all.stream().map(UserBotInvestment::getTotalLoss).filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<BotTradeSummary> recentTrades = tradingHistoryRepository.findByUserBotInvestmentUserIdOrderByOpenedAtDesc(userId)
                .stream().limit(10).map(BotTradeSummary::from).toList();

        return new BotDashboard(all.stream().map(BotInvestmentSummary::from).toList(), active.size(),
                totalInvested, currentBalance, totalProfit, totalLoss, recentTrades);
    }

    @Transactional(readOnly = true)
    public List<BotTradeSummary> history(Long userId, Long investmentId) {
        UserBotInvestment investment = investmentRepository.findById(investmentId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Investment not found."));
        if (!investment.getUser().getId().equals(userId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Unauthorized action.");
        }
        return tradingHistoryRepository.findByUserBotInvestmentIdOrderByOpenedAtDesc(investmentId)
                .stream().map(BotTradeSummary::from).toList();
    }

    private boolean hasActiveInvestment(Long userId, Long botId) {
        return userId != null && investmentRepository.findByUserIdAndBotIdAndStatus(userId, botId, "active").isPresent();
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found."));
    }
}
