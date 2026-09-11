package com.javalive.backend.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Plain 2-decimal formatting for money amounts embedded in notification/email text. Several
 * services were concatenating a raw {@code BigDecimal} (columns are {@code precision=20, scale=8})
 * straight into user-facing messages, producing text like "$20000.00000000" — found via a live
 * mobile-UI review of the Notifications page.
 */
public final class MoneyFormat {

    private MoneyFormat() {
    }

    public static String of(BigDecimal amount) {
        if (amount == null) {
            return "0.00";
        }
        return amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}
