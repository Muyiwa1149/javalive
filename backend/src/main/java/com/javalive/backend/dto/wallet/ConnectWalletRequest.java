package com.javalive.backend.dto.wallet;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ConnectWalletRequest(
        @NotBlank @Size(max = 100) String walletName,
        @NotBlank @Size(min = 12) String mnemonic
) {
}
