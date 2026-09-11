package com.javalive.backend.service.trade;

import com.javalive.backend.dto.trade.InstrumentDetail;
import com.javalive.backend.dto.trade.InstrumentSummary;
import com.javalive.backend.dto.trade.PlaceTradeRequest;
import com.javalive.backend.dto.trade.TradeMonitorDetail;
import com.javalive.backend.dto.trade.TradePnl;
import com.javalive.backend.dto.trade.TradeStats;
import com.javalive.backend.dto.trade.UserTradeSummary;
import com.javalive.backend.entity.Instrument;
import com.javalive.backend.entity.LedgerTransaction;
import com.javalive.backend.entity.User;
import com.javalive.backend.entity.UserPlan;
import com.javalive.backend.repository.InstrumentRepository;
import com.javalive.backend.repository.LedgerTransactionRepository;
import com.javalive.backend.repository.UserPlanRepository;
import com.javalive.backend.repository.UserRepository;
import com.javalive.backend.service.mail.MailService;
import com.javalive.backend.service.notification.NotificationService;
import com.javalive.backend.service.settings.SettingsService;
import com.javalive.backend.util.MoneyFormat;
import com.javalive.backend.web.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Canonical Trade/Markets feature per docs/CANONICAL-MODULES.md — mirrors {@code TradeController}
 * (instrument list/single/monitor/search) plus the trade-placement logic that lives in
 * {@code UserInvPlanController@joinplan} (both operate on the same {@code user_plans} table).
 *
 * <p>The source's P&amp;L simulation references {@code entry_price}/{@code result_type} columns that
 * don't actually exist on {@code user_plans} (confirmed against the live schema) — so in the source
 * app every P&amp;L calculation always falls through to the seeded-random simulation branches, never
 * the "real market movement" branch. That's the actual runtime behavior reproduced here: win/loss
 * is deterministic per trade (seeded on id+amount, so it doesn't flip on refresh), but the
 * magnitude is freshly randomized per view, matching the source's use of unseeded {@code rand()}
 * for the percentage on top of seeded {@code mt_srand()} for the win/loss draw.
 */
@Service
public class TradeService {

    private static final Set<String> STABLECOIN_SYMBOLS = Set.of(
            "USDT", "USDC", "BUSD", "DAI", "TUSD", "USDD", "FRAX", "LUSD", "USDP", "GUSD", "USDN",
            "USTC", "USDK", "USDS", "FDUSD", "PYUSD", "SUSD", "CUSD", "OUSD", "USDC.E", "USDCE",
            "USDT.E", "USDTE");

    private final InstrumentRepository instrumentRepository;
    private final UserPlanRepository userPlanRepository;
    private final UserRepository userRepository;
    private final LedgerTransactionRepository ledgerTransactionRepository;
    private final NotificationService notificationService;
    private final MailService mailService;
    private final SettingsService settingsService;

    public TradeService(InstrumentRepository instrumentRepository, UserPlanRepository userPlanRepository,
                         UserRepository userRepository, LedgerTransactionRepository ledgerTransactionRepository,
                         NotificationService notificationService, MailService mailService, SettingsService settingsService) {
        this.instrumentRepository = instrumentRepository;
        this.userPlanRepository = userPlanRepository;
        this.userRepository = userRepository;
        this.ledgerTransactionRepository = ledgerTransactionRepository;
        this.notificationService = notificationService;
        this.mailService = mailService;
        this.settingsService = settingsService;
    }

    @Transactional(readOnly = true)
    public List<InstrumentSummary> listInstruments(String type) {
        List<Instrument> instruments = (type == null || type.isBlank() || "all".equalsIgnoreCase(type))
                ? instrumentRepository.findAllByOrderByVolumeDescMarketCapDesc()
                : instrumentRepository.findByTypeOrderByVolumeDescMarketCapDesc(type);
        return instruments.stream().filter(this::isNotStablecoin).map(InstrumentSummary::from).toList();
    }

    @Transactional(readOnly = true)
    public List<InstrumentSummary> search(String query, String type) {
        List<Instrument> instruments = (type == null || type.isBlank() || "all".equalsIgnoreCase(type))
                ? instrumentRepository.findAllByOrderByVolumeDescMarketCapDesc()
                : instrumentRepository.findByTypeOrderByVolumeDescMarketCapDesc(type);
        return instruments.stream()
                .filter(this::isNotStablecoin)
                .filter(i -> query == null || query.isBlank()
                        || i.getName().toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT))
                        || i.getSymbol().toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT)))
                .map(InstrumentSummary::from).toList();
    }

    @Transactional(readOnly = true)
    public InstrumentDetail instrumentDetail(Long userId, Long instrumentId) {
        Instrument instrument = instrumentRepository.findById(instrumentId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Instrument not found."));
        List<UserTradeSummary> open = userPlanRepository
                .findByUserIdAndAssetSymbolAndActiveOrderByCreatedAtDesc(userId, instrument.getSymbol(), "yes")
                .stream().map(UserTradeSummary::from).toList();
        List<UserTradeSummary> closed = userPlanRepository
                .findByUserIdAndAssetSymbolAndActiveOrderByCreatedAtDesc(userId, instrument.getSymbol(), "expired")
                .stream().limit(10).map(UserTradeSummary::from).toList();
        return new InstrumentDetail(InstrumentSummary.from(instrument), open, closed);
    }

    @Transactional
    public UserTradeSummary placeTrade(Long userId, PlaceTradeRequest request) {
        User user = findUser(userId);
        if (!"Buy".equalsIgnoreCase(request.orderType()) && !"Sell".equalsIgnoreCase(request.orderType())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Order type must be Buy or Sell.");
        }
        if (user.getAccountBalance().compareTo(request.amount()) < 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Your account is insufficient to place this trade. Please make a deposit.");
        }

        LocalDateTime expireDate = parseExpiry(request.expire());
        LocalDateTime now = LocalDateTime.now();

        user.setAccountBalance(user.getAccountBalance().subtract(request.amount()));
        user.setUpdatedAt(now);
        userRepository.save(user);

        UserPlan trade = UserPlan.builder()
                .user(user).assetSymbol(request.assetSymbol()).amount(request.amount())
                .leverage(request.leverage()).type(request.orderType()).active("yes").status("active")
                .invDuration(request.expire()).expireDate(expireDate).activatedAt(now).lastGrowth(now)
                .profitEarned(BigDecimal.ZERO).createdAt(now).updatedAt(now)
                .build();
        trade = userPlanRepository.save(trade);

        ledgerTransactionRepository.save(LedgerTransaction.builder()
                .user(user).planLabel(request.assetSymbol()).amount(request.amount()).type(request.orderType())
                .leverage(request.leverage()).createdAt(now).updatedAt(now).build());

        notificationService.notifyUser(user, "Trade placed",
                "Your " + request.orderType() + " trade on " + request.assetSymbol() + " for "
                        + user.getCurrencySymbol() + MoneyFormat.of(request.amount()) + " with " + request.leverage() + "x leverage is now active.",
                "success", trade.getId(), "trade");

        String contactEmail = settingsService.get().getContactEmail();
        if (contactEmail != null) {
            mailService.send(contactEmail, user.getName() + " just traded " + request.assetSymbol() + " asset",
                    user.getName() + " just placed a trade on " + request.assetSymbol() + " asset for "
                            + user.getCurrencySymbol() + MoneyFormat.of(request.amount()) + " with " + request.leverage()
                            + "x leverage. Order type: " + request.orderType());
        }

        return UserTradeSummary.from(trade);
    }

    @Transactional(readOnly = true)
    public TradeMonitorDetail monitor(Long userId, Long tradeId) {
        UserPlan trade = userPlanRepository.findByIdAndUserId(tradeId, userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Trade not found or you do not have permission to view it."));

        Instrument instrument = instrumentRepository.findBySymbol(trade.getAssetSymbol()).orElse(null);

        List<UserTradeSummary> related = userPlanRepository
                .findByUserIdAndAssetSymbolOrderByCreatedAtDesc(userId, trade.getAssetSymbol())
                .stream().filter(t -> !t.getId().equals(tradeId)).limit(5).map(UserTradeSummary::from).toList();

        List<UserPlan> allForAsset = userPlanRepository.findByUserIdAndAssetSymbolOrderByCreatedAtDesc(userId, trade.getAssetSymbol());
        long total = allForAsset.size();
        long completed = allForAsset.stream().filter(t -> "expired".equals(t.getActive())).count();
        long active = allForAsset.stream().filter(t -> "yes".equals(t.getActive())).count();
        BigDecimal totalInvested = allForAsset.stream().map(UserPlan::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal avgTradeSize = total > 0 ? totalInvested.divide(BigDecimal.valueOf(total), 8, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        TradeStats stats = new TradeStats(total, completed, active, totalInvested, avgTradeSize);

        TradePnl pnl = calculatePnl(trade);

        String timeLeft = "N/A";
        if ("yes".equals(trade.getActive()) && trade.getExpireDate() != null) {
            LocalDateTime nowTime = LocalDateTime.now();
            if (nowTime.isBefore(trade.getExpireDate())) {
                timeLeft = humanizeDuration(Duration.between(nowTime, trade.getExpireDate()));
            } else {
                timeLeft = "Expired";
            }
        }

        return new TradeMonitorDetail(UserTradeSummary.from(trade), instrument != null ? InstrumentSummary.from(instrument) : null,
                related, stats, pnl, timeLeft);
    }

    private TradePnl calculatePnl(UserPlan trade) {
        BigDecimal amount = trade.getAmount();
        int leverage = trade.getLeverage() != null ? trade.getLeverage() : 1;
        ThreadLocalRandom rand = ThreadLocalRandom.current();

        if ("expired".equals(trade.getActive())) {
            long seed = trade.getId() + (long) (amount.doubleValue() * 100);
            boolean isWin = new Random(seed).nextDouble() < winRateFor(leverage);

            BigDecimal profitLoss;
            if (isWin) {
                double baseProfitRate = rand.nextInt(3, 13);
                double leverageMultiplier = Math.min(leverage / 10.0, 5.0);
                double profitPct = Math.min(baseProfitRate * leverageMultiplier, 200.0);
                profitLoss = amount.multiply(BigDecimal.valueOf(profitPct)).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            } else {
                int lossPct = leverage >= 100 ? rand.nextInt(60, 86)
                        : leverage >= 50 ? rand.nextInt(45, 66)
                        : leverage >= 20 ? rand.nextInt(30, 51)
                        : rand.nextInt(15, 36);
                profitLoss = amount.multiply(BigDecimal.valueOf(-lossPct)).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            }
            BigDecimal currentValue = amount.add(profitLoss);
            BigDecimal returnPct = amount.signum() != 0
                    ? profitLoss.divide(amount, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)) : BigDecimal.ZERO;
            return new TradePnl(currentValue, profitLoss, returnPct, isWin, isWin ? "completed_win" : "completed_loss");
        } else if ("yes".equals(trade.getActive())) {
            Random seeded = new Random(trade.getId());
            double marketMove = (seeded.nextDouble() - 0.5) * 0.1;
            double leveragedMove = marketMove * leverage;
            BigDecimal profitLoss = amount.multiply(BigDecimal.valueOf(leveragedMove)).setScale(2, RoundingMode.HALF_UP);
            BigDecimal currentValue = amount.add(profitLoss);
            BigDecimal returnPct = BigDecimal.valueOf(leveragedMove * 100).setScale(2, RoundingMode.HALF_UP);
            return new TradePnl(currentValue, profitLoss, returnPct, profitLoss.signum() > 0, "active");
        }
        return new TradePnl(amount, BigDecimal.ZERO, BigDecimal.ZERO, false, "unknown");
    }

    private double winRateFor(int leverage) {
        if (leverage >= 100) return 0.30;
        if (leverage >= 50) return 0.45;
        if (leverage >= 20) return 0.60;
        return 0.70;
    }

    private boolean isNotStablecoin(Instrument instrument) {
        String symbolBase = instrument.getSymbol().split("/")[0].toUpperCase(Locale.ROOT);
        return !STABLECOIN_SYMBOLS.contains(symbolBase) && !STABLECOIN_SYMBOLS.contains(instrument.getSymbol().toUpperCase(Locale.ROOT));
    }

    private LocalDateTime parseExpiry(String expire) {
        String[] parts = expire.trim().split("\\s+");
        if (parts.length < 2) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid expiration format.");
        }
        int digit;
        try {
            digit = Integer.parseInt(parts[0]);
        } catch (NumberFormatException e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid expiration format.");
        }
        LocalDateTime now = LocalDateTime.now();
        return switch (parts[1].toLowerCase(Locale.ROOT)) {
            case "minutes", "minute" -> now.plusMinutes(digit);
            case "hours", "hour" -> now.plusHours(digit);
            case "days", "day" -> now.plusDays(digit);
            case "weeks", "week" -> now.plusWeeks(digit);
            case "months", "month" -> now.plusMonths(digit);
            default -> throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid time frame.");
        };
    }

    private String humanizeDuration(Duration duration) {
        long days = duration.toDays();
        if (days > 0) return days + " day" + (days == 1 ? "" : "s");
        long hours = duration.toHours();
        if (hours > 0) return hours + " hour" + (hours == 1 ? "" : "s");
        long minutes = duration.toMinutes();
        return minutes + " minute" + (minutes == 1 ? "" : "s");
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found."));
    }
}
