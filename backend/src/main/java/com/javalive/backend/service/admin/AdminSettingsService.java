package com.javalive.backend.service.admin;

import com.javalive.backend.dto.admin.AdminAppInfoRequest;
import com.javalive.backend.dto.admin.AdminCryptoSettingsRequest;
import com.javalive.backend.dto.admin.AdminPaymentSettingsRequest;
import com.javalive.backend.dto.admin.AdminReferralSettingsRequest;
import com.javalive.backend.dto.admin.AdminSettingsSummary;
import com.javalive.backend.dto.admin.AdminSubscriptionSettingsRequest;
import com.javalive.backend.dto.admin.AdminWithdrawalMethodRequest;
import com.javalive.backend.dto.admin.AdminWithdrawalMethodSummary;
import com.javalive.backend.entity.WithdrawalMethod;
import com.javalive.backend.repository.WithdrawalMethodRepository;
import com.javalive.backend.service.settings.SettingsService;
import com.javalive.backend.service.storage.FileStorageService;
import com.javalive.backend.web.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Mirrors the source app's 5 Settings pages: {@code AppSettingsController} (app info/preferences/
 * email), {@code Settings\ReferralSettings}, {@code Settings\SubscriptionSettings},
 * {@code Settings\PaymentController} (gateway keys + manual/crypto method CRUD), and the
 * crypto-feature toggle from {@code HomeController@managecryptoasset}. All writes go through the
 * shared cache-invalidating {@link SettingsService#update}.
 */
@Service
public class AdminSettingsService {

    private static final List<String> ALLOWED_IMAGE_EXTENSIONS = List.of("jpg", "jpeg", "png", "gif");

    private final SettingsService settingsService;
    private final WithdrawalMethodRepository withdrawalMethodRepository;
    private final FileStorageService fileStorageService;

    public AdminSettingsService(SettingsService settingsService, WithdrawalMethodRepository withdrawalMethodRepository,
                                 FileStorageService fileStorageService) {
        this.settingsService = settingsService;
        this.withdrawalMethodRepository = withdrawalMethodRepository;
        this.fileStorageService = fileStorageService;
    }

    public AdminSettingsSummary get() {
        return AdminSettingsSummary.from(settingsService.get());
    }

    public AdminSettingsSummary updateAppInfo(AdminAppInfoRequest r, MultipartFile logo, MultipartFile favicon) {
        var updated = settingsService.update(s -> {
            s.setSiteName(r.siteName());
            s.setSiteTitle(r.siteTitle());
            s.setSiteAddress(r.siteAddress());
            s.setDescription(r.description());
            s.setKeywords(r.keywords());
            s.setTimezone(r.timezone());
            s.setWelcomeMessage(r.welcomeMessage());
            s.setGoogleTranslateEnabled(r.googleTranslateEnabled());
            s.setTradeMode(r.tradeMode());
            if (r.tradingWinrate() != null) {
                s.setTradingWinrate(r.tradingWinrate());
            }
            s.setMerchantKey(r.merchantKey());
            s.setContactEmail(r.contactEmail());
            s.setDefaultCurrencySymbol(r.defaultCurrencySymbol());
            s.setWeekendTradeEnabled(r.weekendTradeEnabled());
            s.setEnableEmailVerification(r.enableEmailVerification());
            s.setEnableKyc(r.enableKyc());
            s.setEnableKycRegistration(r.enableKycRegistration());
            s.setCaptchaProvider(r.captchaProvider());
            s.setSocialLoginEnabled(r.socialLoginEnabled());
            s.setReturnCapital(r.returnCapital());
            s.setShouldCancelPlan(r.shouldCancelPlan());
            s.setMailServer(r.mailServer());
            s.setMailFromAddress(r.mailFromAddress());
            s.setMailFromName(r.mailFromName());
            s.setSmtpHost(r.smtpHost());
            s.setSmtpPort(r.smtpPort());
            s.setSmtpEncryption(r.smtpEncryption());
            s.setSmtpUsername(r.smtpUsername());
            if (r.smtpPassword() != null && !r.smtpPassword().isBlank()) {
                s.setSmtpPassword(r.smtpPassword());
            }
            s.setGoogleClientId(r.googleClientId());
            s.setGoogleClientSecret(r.googleClientSecret());
            s.setGoogleRedirectUri(r.googleRedirectUri());
            s.setCaptchaSecret(r.captchaSecret());
            s.setCaptchaSiteKey(r.captchaSiteKey());
            if (logo != null && !logo.isEmpty()) {
                fileStorageService.delete(s.getLogo());
                s.setLogo(fileStorageService.storeImage(logo, "photos", ALLOWED_IMAGE_EXTENSIONS));
            }
            if (favicon != null && !favicon.isEmpty()) {
                fileStorageService.delete(s.getFavicon());
                s.setFavicon(fileStorageService.storeImage(favicon, "photos", ALLOWED_IMAGE_EXTENSIONS));
            }
        });
        return AdminSettingsSummary.from(updated);
    }

    public AdminSettingsSummary updateReferral(AdminReferralSettingsRequest r) {
        var updated = settingsService.update(s -> {
            s.setReferralCommissionPct(r.referralCommissionPct());
            s.setReferralCommissionL1(r.referralCommissionL1());
            s.setReferralCommissionL2(r.referralCommissionL2());
            s.setReferralCommissionL3(r.referralCommissionL3());
            s.setReferralCommissionL4(r.referralCommissionL4());
            s.setReferralCommissionL5(r.referralCommissionL5());
            s.setSignupBonus(r.signupBonus());
            s.setDepositBonusPct(r.depositBonusPct());
        });
        return AdminSettingsSummary.from(updated);
    }

    public AdminSettingsSummary updateSubscription(AdminSubscriptionSettingsRequest r) {
        var updated = settingsService.update(s -> {
            s.setSubscriptionMonthlyFee(r.subscriptionMonthlyFee());
            s.setSubscriptionQuarterlyFee(r.subscriptionQuarterlyFee());
            s.setSubscriptionYearlyFee(r.subscriptionYearlyFee());
        });
        return AdminSettingsSummary.from(updated);
    }

    public AdminSettingsSummary updatePayment(AdminPaymentSettingsRequest r) {
        var updated = settingsService.update(s -> {
            s.setWithdrawalOption(r.withdrawalOption());
            s.setDepositOption(r.depositOption());
            s.setAutoMerchantOption(r.autoMerchantOption());
            s.setDeductionOption(r.deductionOption());
            s.setCreditCardProvider(r.creditCardProvider());
            s.setMinTopupAmount(r.minTopupAmount());
            s.setUseInternalTransfer(r.useInternalTransfer());
            s.setMinTransferAmount(r.minTransferAmount());
            s.setTransferCharges(r.transferCharges());
            s.setStripeSecretKey(r.stripeSecretKey());
            s.setStripePublicKey(r.stripePublicKey());
            s.setPaypalClientId(r.paypalClientId());
            s.setPaypalClientSecret(r.paypalClientSecret());
            s.setPaystackPublicKey(r.paystackPublicKey());
            s.setPaystackSecretKey(r.paystackSecretKey());
            s.setPaystackUrl(r.paystackUrl());
            s.setPaystackEmail(r.paystackEmail());
            s.setFlutterwavePublicKey(r.flutterwavePublicKey());
            s.setFlutterwaveSecretKey(r.flutterwaveSecretKey());
            s.setFlutterwaveSecretHash(r.flutterwaveSecretHash());
            s.setBinanceApiKey(r.binanceApiKey());
            s.setBinanceSecretKey(r.binanceSecretKey());
            s.setCoinpaymentsPublicKey(r.coinpaymentsPublicKey());
            s.setCoinpaymentsPrivateKey(r.coinpaymentsPrivateKey());
            s.setCoinpaymentsMerchantId(r.coinpaymentsMerchantId());
            s.setCoinpaymentsIpnSecret(r.coinpaymentsIpnSecret());
            s.setCoinpaymentsDebugEmail(r.coinpaymentsDebugEmail());
        });
        return AdminSettingsSummary.from(updated);
    }

    public AdminSettingsSummary updateCrypto(AdminCryptoSettingsRequest r) {
        var updated = settingsService.update(s -> {
            s.setUseCryptoFeature(r.useCryptoFeature());
            s.setExchangeFeePct(r.exchangeFeePct());
            s.setCurrencyRate(r.currencyRate());
            s.setLocalCurrency(r.localCurrency());
            s.setBaseCurrency(r.baseCurrency());
        });
        return AdminSettingsSummary.from(updated);
    }

    @Transactional(readOnly = true)
    public List<AdminWithdrawalMethodSummary> listMethods() {
        return withdrawalMethodRepository.findAllByOrderByIdDesc().stream().map(AdminWithdrawalMethodSummary::from).toList();
    }

    @Transactional
    public AdminWithdrawalMethodSummary createMethod(AdminWithdrawalMethodRequest r, MultipartFile barcode) {
        WithdrawalMethod method = WithdrawalMethod.builder()
                .name(r.name()).methodType(r.methodType()).type(r.type()).minimumAmount(r.minimumAmount())
                .maximumAmount(r.maximumAmount()).chargesAmount(r.chargesAmount()).chargesType(r.chargesType())
                .durationNote(r.durationNote()).imageUrl(r.imageUrl()).bankName(r.bankName())
                .accountName(r.accountName()).accountNumber(r.accountNumber()).swiftCode(r.swiftCode())
                .walletAddress(r.walletAddress()).network(r.network()).isDefault(false).status(r.status())
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();
        if (barcode != null && !barcode.isEmpty()) {
            method.setBarcodeImage(fileStorageService.storeImage(barcode, "photos", ALLOWED_IMAGE_EXTENSIONS));
        }
        return AdminWithdrawalMethodSummary.from(withdrawalMethodRepository.save(method));
    }

    @Transactional
    public AdminWithdrawalMethodSummary updateMethod(Long id, AdminWithdrawalMethodRequest r, MultipartFile barcode) {
        WithdrawalMethod method = getMethod(id);
        method.setName(r.name());
        method.setMethodType(r.methodType());
        method.setType(r.type());
        method.setMinimumAmount(r.minimumAmount());
        method.setMaximumAmount(r.maximumAmount());
        method.setChargesAmount(r.chargesAmount());
        method.setChargesType(r.chargesType());
        method.setDurationNote(r.durationNote());
        method.setImageUrl(r.imageUrl());
        method.setBankName(r.bankName());
        method.setAccountName(r.accountName());
        method.setAccountNumber(r.accountNumber());
        method.setSwiftCode(r.swiftCode());
        method.setWalletAddress(r.walletAddress());
        method.setNetwork(r.network());
        method.setStatus(r.status());
        method.setUpdatedAt(LocalDateTime.now());
        if (barcode != null && !barcode.isEmpty()) {
            fileStorageService.delete(method.getBarcodeImage());
            method.setBarcodeImage(fileStorageService.storeImage(barcode, "photos", ALLOWED_IMAGE_EXTENSIONS));
        }
        return AdminWithdrawalMethodSummary.from(withdrawalMethodRepository.save(method));
    }

    @Transactional
    public void toggleMethodStatus(Long id) {
        WithdrawalMethod method = getMethod(id);
        method.setStatus("enabled".equals(method.getStatus()) ? "disabled" : "enabled");
        method.setUpdatedAt(LocalDateTime.now());
        withdrawalMethodRepository.save(method);
    }

    @Transactional
    public void deleteMethod(Long id) {
        withdrawalMethodRepository.delete(getMethod(id));
    }

    private WithdrawalMethod getMethod(Long id) {
        return withdrawalMethodRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Payment method not found."));
    }
}
