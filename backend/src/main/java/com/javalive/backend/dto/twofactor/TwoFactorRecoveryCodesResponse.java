package com.javalive.backend.dto.twofactor;

import java.util.List;

/** Plaintext codes are only ever returned here — once, right after generation. They're stored encrypted
 *  and are never retrievable in plaintext again after this response. */
public record TwoFactorRecoveryCodesResponse(List<String> recoveryCodes) {
}
