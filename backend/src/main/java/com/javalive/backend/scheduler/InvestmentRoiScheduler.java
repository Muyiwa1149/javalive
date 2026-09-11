package com.javalive.backend.scheduler;

import com.javalive.backend.entity.AppSetting;
import com.javalive.backend.entity.Investment;
import com.javalive.backend.entity.LedgerTransaction;
import com.javalive.backend.entity.Plan;
import com.javalive.backend.entity.User;
import com.javalive.backend.repository.InvestmentRepository;
import com.javalive.backend.repository.LedgerTransactionRepository;
import com.javalive.backend.repository.UserRepository;
import com.javalive.backend.service.mail.MailService;
import com.javalive.backend.service.settings.SettingsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Java port of the source app's {@code ProcessInvestmentPlanRoi} console command (signature
 * {@code investments:process-roi}) — confirmed as the canonical, actually-scheduled ROI job by
 * reading {@code app/Console/Kernel.php} directly (its sibling {@code ProcessInvestmentRoi} is
 * registered but never scheduled — dead code, not ported). See {@code docs/CANONICAL-MODULES.md}.
 */
@Component
public class InvestmentRoiScheduler {

    private static final Logger log = LoggerFactory.getLogger(InvestmentRoiScheduler.class);

    private static final Pattern MINUTES = Pattern.compile("(\\d+)\\s*Minutes?", Pattern.CASE_INSENSITIVE);
    private static final Pattern HOURS = Pattern.compile("(\\d+)\\s*Hours?", Pattern.CASE_INSENSITIVE);
    private static final Pattern DAYS = Pattern.compile("(\\d+)\\s*Days?", Pattern.CASE_INSENSITIVE);
    private static final Pattern WEEKS = Pattern.compile("(\\d+)\\s*Weeks?", Pattern.CASE_INSENSITIVE);
    private static final Pattern MONTHS = Pattern.compile("(\\d+)\\s*Months?", Pattern.CASE_INSENSITIVE);

    private final InvestmentRepository investmentRepository;
    private final UserRepository userRepository;
    private final LedgerTransactionRepository ledgerTransactionRepository;
    private final SettingsService settingsService;
    private final MailService mailService;

    public InvestmentRoiScheduler(InvestmentRepository investmentRepository,
                                   UserRepository userRepository,
                                   LedgerTransactionRepository ledgerTransactionRepository,
                                   SettingsService settingsService,
                                   MailService mailService) {
        this.investmentRepository = investmentRepository;
        this.userRepository = userRepository;
        this.ledgerTransactionRepository = ledgerTransactionRepository;
        this.settingsService = settingsService;
        this.mailService = mailService;
    }

    /** {@code ->everyFiveMinutes()->withoutOverlapping()} — fixedDelay naturally serializes runs. */
    @Scheduled(initialDelay = 30_000, fixedDelay = 300_000)
    @Transactional
    public void processRoi() {
        AppSetting settings = settingsService.get();
        if (settings == null || !"on".equals(settings.getTradeMode())) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        boolean isWeekend = now.getDayOfWeek() == DayOfWeek.SATURDAY || now.getDayOfWeek() == DayOfWeek.SUNDAY;
        boolean weekendTradeOn = Boolean.TRUE.equals(settings.getWeekendTradeEnabled());

        List<Investment> investments = investmentRepository.findByActiveWithUserAndPlanOrderByIdDesc("yes");
        int processed = 0;
        int completed = 0;

        for (Investment investment : investments) {
            try {
                Plan plan = investment.getPlan();
                User user = investment.getUser();
                if (plan == null || user == null) {
                    continue;
                }

                if (investment.getExpireDate() != null && !now.isBefore(investment.getExpireDate())) {
                    completeInvestment(investment, user, plan, settings, now);
                    completed++;
                    continue;
                }

                if (isWeekend && !weekendTradeOn) {
                    continue;
                }

                LocalDateTime lastGrowth = investment.getLastGrowth() != null
                        ? investment.getLastGrowth()
                        : (investment.getActivatedAt() != null ? investment.getActivatedAt() : investment.getCreatedAt());
                LocalDateTime nextDue = calculateNextPayoutDate(lastGrowth, plan.getIncrementInterval());

                if (now.isBefore(nextDue)) {
                    continue;
                }

                BigDecimal profit = calculateProfit(investment, plan);
                if (profit.signum() <= 0) {
                    continue;
                }

                investment.setProfitEarned(investment.getProfitEarned().add(profit));
                investment.setLastGrowth(now);
                investmentRepository.save(investment);

                user.setRoiBalance(user.getRoiBalance().add(profit));
                userRepository.save(user);

                ledgerTransactionRepository.save(LedgerTransaction.builder()
                        .user(user)
                        .planLabel(plan.getName())
                        .investment(investment)
                        .amount(profit)
                        .type("ROI")
                        .createdAt(now)
                        .updatedAt(now)
                        .build());

                if (Boolean.TRUE.equals(user.getSendRoiEmail())) {
                    String body = String.format(
                            "Your %s investment plan has generated a new return of %s%s, credited to your account on %s.",
                            plan.getName(), nullToEmpty(user.getCurrencySymbol()), formatAmount(profit),
                            now.format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")));
                    mailService.send(user.getEmail(), "New Return on Investment (ROI)", body);
                }

                processed++;
            } catch (Exception e) {
                log.error("Error processing ROI for investment #{}: {}", investment.getId(), e.getMessage());
            }
        }

        if (processed > 0 || completed > 0) {
            log.info("ROI processed for {} active investments. {} investments completed.", processed, completed);
        }
    }

    private BigDecimal calculateProfit(Investment investment, Plan plan) {
        BigDecimal amount = investment.getAmount();
        BigDecimal rate = plan.getIncrementAmount() != null ? plan.getIncrementAmount() : BigDecimal.ZERO;

        if ("Percentage".equals(plan.getIncrementType())) {
            return amount.multiply(rate).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }

        return rate.setScale(2, RoundingMode.HALF_UP);
    }

    private LocalDateTime calculateNextPayoutDate(LocalDateTime lastGrowth, String interval) {
        String value = interval == null ? "" : interval.trim();

        Matcher m = MINUTES.matcher(value);
        if (m.find()) return lastGrowth.plusMinutes(Long.parseLong(m.group(1)));

        m = HOURS.matcher(value);
        if (m.find()) return lastGrowth.plusHours(Long.parseLong(m.group(1)));

        m = DAYS.matcher(value);
        if (m.find()) return lastGrowth.plusDays(Long.parseLong(m.group(1)));

        m = WEEKS.matcher(value);
        if (m.find()) return lastGrowth.plusWeeks(Long.parseLong(m.group(1)));

        m = MONTHS.matcher(value);
        if (m.find()) return lastGrowth.plusMonths(Long.parseLong(m.group(1)));

        return switch (value.toLowerCase()) {
            case "hourly" -> lastGrowth.plusHours(1);
            case "daily" -> lastGrowth.plusDays(1);
            case "weekly" -> lastGrowth.plusWeeks(1);
            case "bi-weekly", "biweekly", "bi weekly" -> lastGrowth.plusWeeks(2);
            case "monthly" -> lastGrowth.plusMonths(1);
            case "quarterly" -> lastGrowth.plusMonths(3);
            case "yearly", "annually" -> lastGrowth.plusYears(1);
            default -> lastGrowth.plusDays(1);
        };
    }

    private void completeInvestment(Investment investment, User user, Plan plan, AppSetting settings, LocalDateTime now) {
        if (Boolean.TRUE.equals(settings.getReturnCapital())) {
            BigDecimal capital = investment.getAmount();
            user.setAccountBalance(user.getAccountBalance().add(capital));
            userRepository.save(user);

            ledgerTransactionRepository.save(LedgerTransaction.builder()
                    .user(user)
                    .planLabel(plan.getName())
                    .investment(investment)
                    .amount(capital)
                    .type("Investment capital return")
                    .createdAt(now)
                    .updatedAt(now)
                    .build());
        }

        investment.setActive("expired");
        investmentRepository.save(investment);

        if (Boolean.TRUE.equals(user.getSendInvPlanEmail())) {
            String message = String.format("Your %s investment plan of %s%s has matured and is now complete.",
                    plan.getName(), nullToEmpty(user.getCurrencySymbol()), formatAmount(investment.getAmount()));
            mailService.send(user.getEmail(), "Investment Plan Completed", message);
        }
    }

    private String formatAmount(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private String nullToEmpty(String value) {
        return value != null ? value : "";
    }
}
