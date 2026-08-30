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

/** Mirrors the source app's ViewsController@accounthistory — general ledger (excluding leveraged trade entries), deposits, withdrawals. */
@Service
public class AccountHistoryService {

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
        return new AccountHistory(transactions, deposits, withdrawals);
    }
}
