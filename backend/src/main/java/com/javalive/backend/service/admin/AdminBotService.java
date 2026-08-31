package com.javalive.backend.service.admin;

import tools.jackson.databind.ObjectMapper;
import com.javalive.backend.dto.admin.AdminBotAnalytics;
import com.javalive.backend.dto.admin.AdminBotDetail;
import com.javalive.backend.dto.admin.AdminBotRequest;
import com.javalive.backend.dto.admin.AdminBotSummary;
import com.javalive.backend.dto.admin.AdminBotTradeSummary;
import com.javalive.backend.dto.admin.AdminBotsDashboard;
import com.javalive.backend.entity.TradingBot;
import com.javalive.backend.repository.BotTradingHistoryRepository;
import com.javalive.backend.repository.TradingBotRepository;
import com.javalive.backend.repository.UserBotInvestmentRepository;
import com.javalive.backend.service.storage.FileStorageService;
import com.javalive.backend.web.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Mirrors the source app's {@code Admin\BotController} exactly. */
@Service
public class AdminBotService {

    private static final List<String> ALLOWED_IMAGE_EXTENSIONS = List.of("jpg", "jpeg", "png", "gif");

    private final TradingBotRepository botRepository;
    private final UserBotInvestmentRepository investmentRepository;
    private final BotTradingHistoryRepository historyRepository;
    private final FileStorageService fileStorageService;
    private final ObjectMapper objectMapper;

    public AdminBotService(TradingBotRepository botRepository, UserBotInvestmentRepository investmentRepository,
                            BotTradingHistoryRepository historyRepository, FileStorageService fileStorageService,
                            ObjectMapper objectMapper) {
        this.botRepository = botRepository;
        this.investmentRepository = investmentRepository;
        this.historyRepository = historyRepository;
        this.fileStorageService = fileStorageService;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public List<AdminBotSummary> list() {
        return botRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(b -> AdminBotSummary.from(b, investmentRepository.countByBotId(b.getId()),
                        investmentRepository.countByBotIdAndStatus(b.getId(), "active")))
                .toList();
    }

    @Transactional
    public AdminBotSummary create(AdminBotRequest request, MultipartFile image) {
        if (botRepository.existsByNameIgnoreCase(request.name())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "A bot with this name already exists.");
        }
        validateRanges(request);

        TradingBot bot = TradingBot.builder()
                .name(request.name()).botType(request.botType()).description(request.description())
                .minInvestment(request.minInvestment()).maxInvestment(request.maxInvestment())
                .dailyProfitMin(request.dailyProfitMin()).dailyProfitMax(request.dailyProfitMax())
                .successRate(request.successRate()).durationDays(request.durationDays())
                .totalEarned(BigDecimal.ZERO).totalUsers(0).status(request.status())
                .tradingPairs(toJson(request.tradingPairs()))
                .riskSettings(toJson(Map.of("stop_loss", 5, "take_profit", 10, "max_trades_per_day", 5, "risk_per_trade", 2)))
                .strategyDetails(toJson(Map.of("algorithm", "Advanced AI Algorithm", "timeframe", "1H")))
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();
        if (image != null && !image.isEmpty()) {
            bot.setImage(fileStorageService.storeImage(image, "bots", ALLOWED_IMAGE_EXTENSIONS));
        }
        bot = botRepository.save(bot);
        return AdminBotSummary.from(bot, 0, 0);
    }

    @Transactional
    public AdminBotSummary update(Long id, AdminBotRequest request, MultipartFile image) {
        TradingBot bot = getBot(id);
        if (botRepository.existsByNameIgnoreCaseAndIdNot(request.name(), id)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "A bot with this name already exists.");
        }
        validateRanges(request);

        bot.setName(request.name());
        bot.setBotType(request.botType());
        bot.setDescription(request.description());
        bot.setMinInvestment(request.minInvestment());
        bot.setMaxInvestment(request.maxInvestment());
        bot.setDailyProfitMin(request.dailyProfitMin());
        bot.setDailyProfitMax(request.dailyProfitMax());
        bot.setSuccessRate(request.successRate());
        bot.setDurationDays(request.durationDays());
        bot.setStatus(request.status());
        bot.setTradingPairs(toJson(request.tradingPairs()));
        bot.setUpdatedAt(LocalDateTime.now());
        if (image != null && !image.isEmpty()) {
            fileStorageService.delete(bot.getImage());
            bot.setImage(fileStorageService.storeImage(image, "bots", ALLOWED_IMAGE_EXTENSIONS));
        }
        bot = botRepository.save(bot);
        return AdminBotSummary.from(bot, investmentRepository.countByBotId(id), investmentRepository.countByBotIdAndStatus(id, "active"));
    }

    @Transactional
    public void toggleStatus(Long id) {
        TradingBot bot = getBot(id);
        bot.setStatus("active".equals(bot.getStatus()) ? "inactive" : "active");
        bot.setUpdatedAt(LocalDateTime.now());
        botRepository.save(bot);
    }

    @Transactional
    public void delete(Long id) {
        TradingBot bot = getBot(id);
        if (investmentRepository.countByBotIdAndStatus(id, "active") > 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Cannot delete bot with active investments. Please wait for all investments to complete.");
        }
        fileStorageService.delete(bot.getImage());
        botRepository.delete(bot);
    }

    @Transactional(readOnly = true)
    public AdminBotDetail detail(Long id) {
        TradingBot bot = getBot(id);
        long activeInvestments = investmentRepository.countByBotIdAndStatus(id, "active");
        BigDecimal totalInvested = investmentRepository.sumInvestmentAmountByBotId(id);
        BigDecimal totalProfits = investmentRepository.sumTotalProfitByBotId(id);
        double avgSuccess = investmentRepository.avgSuccessfulTradesByBotId(id);
        double avgFail = investmentRepository.avgFailedTradesByBotId(id);
        double totalTrades = avgSuccess + avgFail;
        double avgSuccessRate = totalTrades > 0 ? (avgSuccess / totalTrades) * 100 : 0;

        List<AdminBotTradeSummary> recentTrades = historyRepository.findByUserBotInvestment_Bot_IdOrderByOpenedAtDesc(id)
                .stream().limit(20).map(AdminBotTradeSummary::from).toList();

        return new AdminBotDetail(
                AdminBotSummary.from(bot, investmentRepository.countByBotId(id), activeInvestments),
                investmentRepository.countDistinctUsersByBotId(id), activeInvestments, totalInvested, totalProfits,
                avgSuccessRate, recentTrades
        );
    }

    @Transactional(readOnly = true)
    public AdminBotsDashboard dashboard() {
        LocalDateTime since = LocalDateTime.now().minusDays(29).toLocalDate().atStartOfDay();
        List<AdminBotsDashboard.AdminTopBot> topBots = botRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(b -> new AdminBotsDashboard.AdminTopBot(b.getId(), b.getName(),
                        investmentRepository.countByBotId(b.getId()), investmentRepository.countByBotIdAndStatus(b.getId(), "active"),
                        investmentRepository.sumTotalProfitByBotId(b.getId())))
                .sorted((a, c) -> Long.compare(c.investmentsCount(), a.investmentsCount()))
                .limit(5)
                .toList();

        return new AdminBotsDashboard(
                botRepository.count(), botRepository.countByStatus("active"),
                investmentRepository.sumInvestmentAmount(), investmentRepository.sumTotalProfit(),
                investmentRepository.countByStatus("active"), investmentRepository.countByStatus("completed"),
                topBots, dailyProfitSeries(historyRepository.dailyProfitAll(since))
        );
    }

    @Transactional(readOnly = true)
    public AdminBotAnalytics analytics(Long id) {
        getBot(id);
        LocalDateTime since = LocalDateTime.now().minusDays(29).toLocalDate().atStartOfDay();
        return new AdminBotAnalytics(
                dailyProfitSeries(historyRepository.dailyProfitByBotId(id, since)),
                historyRepository.countByUserBotInvestment_Bot_Id(id),
                historyRepository.countByUserBotInvestment_Bot_IdAndResult(id, "profit")
        );
    }

    private List<AdminBotsDashboard.AdminDailyProfit> dailyProfitSeries(List<Object[]> rows) {
        Map<String, BigDecimal> byDate = new LinkedHashMap<>();
        DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE;
        for (int i = 29; i >= 0; i--) {
            byDate.put(LocalDate.now().minusDays(i).format(fmt), BigDecimal.ZERO);
        }
        for (Object[] row : rows) {
            String date = row[0].toString();
            BigDecimal total = row[1] instanceof BigDecimal bd ? bd : new BigDecimal(row[1].toString());
            if (byDate.containsKey(date)) {
                byDate.put(date, total);
            }
        }
        return byDate.entrySet().stream().map(e -> new AdminBotsDashboard.AdminDailyProfit(e.getKey(), e.getValue())).toList();
    }

    private void validateRanges(AdminBotRequest request) {
        if (request.minInvestment().compareTo(request.maxInvestment()) >= 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Maximum investment must be greater than minimum investment.");
        }
        if (request.dailyProfitMin().compareTo(request.dailyProfitMax()) >= 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Maximum daily profit must be greater than minimum daily profit.");
        }
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to encode bot configuration.");
        }
    }

    private TradingBot getBot(Long id) {
        return botRepository.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Trading bot not found."));
    }
}
