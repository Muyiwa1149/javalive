package com.javalive.backend.service.history;

import com.javalive.backend.dto.deposit.DepositSummary;
import com.javalive.backend.dto.history.AccountHistory;
import com.javalive.backend.dto.history.LedgerEntrySummary;
import com.javalive.backend.dto.withdrawal.WithdrawalSummary;
import com.javalive.backend.repository.DepositRepository;
import com.javalive.backend.repository.LedgerTransactionRepository;
import com.javalive.backend.repository.WithdrawalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Mirrors the source app's ViewsController@accounthistory (general ledger excluding leveraged
 * trade entries, deposits, withdrawals) AND @tradinghistory (Sell/Buy/WIN/LOSE leveraged trade
 * entries) as one combined endpoint — same underlying data, consolidated into one page with tabs
 * instead of a separate nav entry (the source's own "Performance History" link was itself
 * commented out of the sidebar, so this loses no reachability).
 */
@Service
public class AccountHistoryService {

    private static final List<String> TRADE_TYPES = List.of("Sell", "Buy", "WIN", "LOSE");

    private final LedgerTransactionRepository ledgerTransactionRepository;
    private final DepositRepository depositRepository;
    private final WithdrawalRepository withdrawalRepository;

    public AccountHistoryService(LedgerTransactionRepository ledgerTransactionRepository,
                                  DepositRepository depositRepository, WithdrawalRepository withdrawalRepository) {
        this.ledgerTransactionRepository = ledgerTransactionRepository;
        this.depositRepository = depositRepository;
        this.withdrawalRepository = withdrawalRepository;
    }

    @Transactional(readOnly = true)
    public AccountHistory history(Long userId) {
        var transactions = ledgerTransactionRepository.findByUserIdAndLeverageIsNullOrderByIdDesc(userId)
                .stream().map(LedgerEntrySummary::from).toList();
        var deposits = depositRepository.findByUserIdOrderByIdDesc(userId).stream().map(DepositSummary::from).toList();
        var withdrawals = withdrawalRepository.findByUserIdOrderByIdDesc(userId).stream().map(WithdrawalSummary::from).toList();
        var trades = ledgerTransactionRepository.findByUserIdAndTypeInOrderByIdDesc(userId, TRADE_TYPES)
                .stream().map(LedgerEntrySummary::from).toList();
        return new AccountHistory(transactions, deposits, withdrawals, trades);
    }
}
