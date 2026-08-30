package com.javalive.backend.service.finance;

import com.javalive.backend.entity.AppSetting;
import com.javalive.backend.entity.LedgerTransaction;
import com.javalive.backend.entity.User;
import com.javalive.backend.repository.LedgerTransactionRepository;
import com.javalive.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Credits the referral commission chain on a qualifying deposit — mirrors the source app's
 * {@code Controller::getAncestors} recursion, reimplemented as a direct chain walk instead of a
 * full-table scan (same result, more efficient). The direct referrer gets a flat
 * {@code referralCommissionPct}; the next 5 ancestors above them get L1..L5 respectively — six
 * commission levels total per deposit, matching the six percentage fields on {@link AppSetting}.
 */
@Service
public class ReferralCommissionService {

    private final UserRepository userRepository;
    private final LedgerTransactionRepository ledgerTransactionRepository;

    public ReferralCommissionService(UserRepository userRepository, LedgerTransactionRepository ledgerTransactionRepository) {
        this.userRepository = userRepository;
        this.ledgerTransactionRepository = ledgerTransactionRepository;
    }

    public void creditChain(User depositor, BigDecimal depositAmount, AppSetting settings) {
        User directReferrer = resolveReferrer(depositor);
        if (directReferrer == null) {
            return;
        }
        credit(directReferrer, depositAmount, settings.getReferralCommissionPct());

        List<BigDecimal> ancestorRates = List.of(
                settings.getReferralCommissionL1(), settings.getReferralCommissionL2(),
                settings.getReferralCommissionL3(), settings.getReferralCommissionL4(),
                settings.getReferralCommissionL5());

        User current = directReferrer;
        for (BigDecimal rate : ancestorRates) {
            User ancestor = resolveReferrer(current);
            if (ancestor == null) {
                break;
            }
            credit(ancestor, depositAmount, rate);
            current = ancestor;
        }
    }

    private User resolveReferrer(User user) {
        String code = user.getReferredByCode();
        if (code == null || code.isBlank()) {
            return null;
        }
        try {
            return userRepository.findById(Long.parseLong(code)).orElse(null);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void credit(User recipient, BigDecimal depositAmount, BigDecimal ratePct) {
        if (ratePct == null || ratePct.signum() <= 0) {
            return;
        }
        BigDecimal earnings = depositAmount.multiply(ratePct)
                .divide(BigDecimal.valueOf(100), 8, RoundingMode.HALF_UP);
        if (earnings.signum() <= 0) {
            return;
        }
        recipient.setAccountBalance(recipient.getAccountBalance().add(earnings));
        recipient.setReferralBonusBalance(recipient.getReferralBonusBalance().add(earnings));
        recipient.setUpdatedAt(LocalDateTime.now());
        userRepository.save(recipient);

        ledgerTransactionRepository.save(LedgerTransaction.builder()
                .user(recipient).planLabel("Credit").amount(earnings).type("Ref_bonus")
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build());
    }
}
