package com.javalive.backend.service.admin;

import com.javalive.backend.dto.admin.AdminWalletSettingsRequest;
import com.javalive.backend.dto.admin.AdminWalletSettingsSummary;
import com.javalive.backend.dto.admin.AdminWalletSummary;
import com.javalive.backend.entity.Wallet;
import com.javalive.backend.repository.WalletRepository;
import com.javalive.backend.service.settings.SettingsService;
import com.javalive.backend.service.wallet.AesEncryptionService;
import com.javalive.backend.web.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Mirrors the source app's {@code HomeController@mwalletconnect}/{@code mwalletsettings}/
 * {@code mwalletdelete}/{@code mwalletconnectsave} — the on-demand decrypt-and-view action here is
 * the Phase 5 half of the Phase 4 security decision (phrase encrypted at rest, never emailed;
 * viewable only through this explicit admin action).
 */
@Service
public class AdminWalletService {

    private final WalletRepository walletRepository;
    private final AesEncryptionService encryptionService;
    private final SettingsService settingsService;

    public AdminWalletService(WalletRepository walletRepository, AesEncryptionService encryptionService,
                               SettingsService settingsService) {
        this.walletRepository = walletRepository;
        this.encryptionService = encryptionService;
        this.settingsService = settingsService;
    }

    @Transactional(readOnly = true)
    public List<AdminWalletSummary> list() {
        return walletRepository.findAllWithUserOrderByIdDesc().stream().map(AdminWalletSummary::from).toList();
    }

    @Transactional(readOnly = true)
    public Map<String, String> revealPhrase(Long id) {
        Wallet wallet = getWallet(id);
        return Map.of("phrase", encryptionService.decrypt(wallet.getPhraseEncrypted()));
    }

    @Transactional
    public void delete(Long id) {
        walletRepository.delete(getWallet(id));
    }

    public AdminWalletSettingsSummary getSettings() {
        var s = settingsService.get();
        return new AdminWalletSettingsSummary(s.getMinBalance(), s.getMinReturn(), s.getWalletStatus());
    }

    public AdminWalletSettingsSummary updateSettings(AdminWalletSettingsRequest r) {
        var updated = settingsService.update(s -> {
            s.setMinBalance(r.minBalance());
            s.setMinReturn(r.minReturn());
            s.setWalletStatus(r.walletStatus());
        });
        return new AdminWalletSettingsSummary(updated.getMinBalance(), updated.getMinReturn(), updated.getWalletStatus());
    }

    private Wallet getWallet(Long id) {
        return walletRepository.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Wallet not found."));
    }
}
