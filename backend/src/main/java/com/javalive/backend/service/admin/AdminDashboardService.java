package com.javalive.backend.service.admin;

import com.javalive.backend.dto.admin.AdminDashboardSummary;
import com.javalive.backend.repository.DepositRepository;
import com.javalive.backend.repository.LedgerTransactionRepository;
import com.javalive.backend.repository.PlanRepository;
import com.javalive.backend.repository.UserRepository;
import com.javalive.backend.repository.WithdrawalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}
