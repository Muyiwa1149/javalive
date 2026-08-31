package com.javalive.backend.dto.history;

import com.javalive.backend.dto.deposit.DepositSummary;
import com.javalive.backend.dto.withdrawal.WithdrawalSummary;

import java.util.List;

public record AccountHistory(
        List<LedgerEntrySummary> transactions, List<DepositSummary> deposits, List<WithdrawalSummary> withdrawals,
        List<LedgerEntrySummary> trades
) {
}
