package com.javalive.backend.service.admin;

import com.javalive.backend.dto.admin.AdminDashboardSeries;
import com.javalive.backend.dto.admin.AdminDashboardSummary;
import com.javalive.backend.repository.DepositRepository;
import com.javalive.backend.repository.LedgerTransactionRepository;
import com.javalive.backend.repository.PlanRepository;
import com.javalive.backend.repository.UserRepository;
import com.javalive.backend.repository.WithdrawalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Mirrors the source app's {@code Admin\HomeController@index} exactly. */
@Service
public class AdminDashboardService {

    private final DepositRepository depositRepository;
    private final WithdrawalRepository withdrawalRepository;
    private final UserRepository userRepository;
    private final PlanRepository planRepository;
    private final LedgerTransactionRepository ledgerTransactionRepository;

    public AdminDashboardService(DepositRepository depositRepository, WithdrawalRepository withdrawalRepository,
                                  UserRepository userRepository, PlanRepository planRepository,
                                  LedgerTransactionRepository ledgerTransactionRepository) {
        this.depositRepository = depositRepository;
        this.withdrawalRepository = withdrawalRepository;
        this.userRepository = userRepository;
        this.planRepository = planRepository;
        this.ledgerTransactionRepository = ledgerTransactionRepository;
    }

    @Transactional(readOnly = true)
    public AdminDashboardSummary summary() {
        var totalDeposited = depositRepository.sumAmountByStatus("Processed");
        var pendingDeposited = depositRepository.sumAmountByStatus("Pending");
        var totalWithdrawn = withdrawalRepository.sumAmountByStatus("Processed");
        var pendingWithdrawn = withdrawalRepository.sumAmountByStatus("Pending");
        var transactions = ledgerTransactionRepository.sumAmount();

        return new AdminDashboardSummary(
                totalDeposited, pendingDeposited, totalWithdrawn, pendingWithdrawn,
                userRepository.count(), userRepository.countByStatus("active"), userRepository.countByStatus("blocked"),
                planRepository.count(),
                totalDeposited, pendingDeposited, totalWithdrawn, pendingWithdrawn, transactions
        );
    }

    /**
     * Real 30-day deposits-vs-withdrawals trend, replacing a "chart" that previously just re-displayed
     * the same 4 all-time totals already shown as stat cards above it, with no time dimension at all.
     */
    @Transactional(readOnly = true)
    public AdminDashboardSeries series() {
        LocalDateTime since = LocalDateTime.now().minusDays(29).toLocalDate().atStartOfDay();
        return new AdminDashboardSeries(
                dailySeries(depositRepository.dailyProcessedTotals(since)),
                dailySeries(withdrawalRepository.dailyProcessedTotals(since))
        );
    }

    private List<AdminDashboardSeries.AdminDailyPoint> dailySeries(List<Object[]> rows) {
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
        return byDate.entrySet().stream()
                .map(e -> new AdminDashboardSeries.AdminDailyPoint(e.getKey(), e.getValue()))
                .toList();
    }
}
