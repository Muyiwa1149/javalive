package com.javalive.backend.web.admin;

import com.javalive.backend.dto.admin.AdminAppInfoRequest;
import com.javalive.backend.dto.admin.AdminCryptoSettingsRequest;
import com.javalive.backend.dto.admin.AdminPaymentSettingsRequest;
import com.javalive.backend.dto.admin.AdminReferralSettingsRequest;
import com.javalive.backend.dto.admin.AdminSettingsSummary;
import com.javalive.backend.dto.admin.AdminSubscriptionSettingsRequest;
import com.javalive.backend.dto.admin.AdminWithdrawalMethodRequest;
import com.javalive.backend.dto.admin.AdminWithdrawalMethodSummary;
import com.javalive.backend.service.admin.AdminSettingsService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/admin/settings")
public class AdminSettingsController {

    private final AdminSettingsService adminSettingsService;

    public AdminSettingsController(AdminSettingsService adminSettingsService) {
        this.adminSettingsService = adminSettingsService;
    }

    @GetMapping
    public AdminSettingsSummary get() {
        return adminSettingsService.get();
    }

    @PostMapping(value = "/app", consumes = "multipart/form-data")
    public AdminSettingsSummary updateAppInfo(
            @RequestParam String siteName, @RequestParam String siteTitle, @RequestParam String siteAddress,
            @RequestParam(required = false) String description, @RequestParam(required = false) String keywords,
            @RequestParam String timezone, @RequestParam(required = false) String welcomeMessage,
            @RequestParam boolean googleTranslateEnabled, @RequestParam String tradeMode,
            @RequestParam(required = false) Integer tradingWinrate, @RequestParam(required = false) String merchantKey,
            @RequestParam(required = false) String contactEmail, @RequestParam(required = false) String defaultCurrencySymbol,
            @RequestParam boolean weekendTradeEnabled, @RequestParam boolean enableEmailVerification,
            @RequestParam boolean enableKyc, @RequestParam boolean enableKycRegistration,
            @RequestParam(required = false) String captchaProvider, @RequestParam boolean socialLoginEnabled,
            @RequestParam boolean returnCapital, @RequestParam boolean shouldCancelPlan,
            @RequestParam(required = false) String mailServer, @RequestParam(required = false) String mailFromAddress,
            @RequestParam(required = false) String mailFromName, @RequestParam(required = false) String smtpHost,
            @RequestParam(required = false) String smtpPort, @RequestParam(required = false) String smtpEncryption,
            @RequestParam(required = false) String smtpUsername, @RequestParam(required = false) String smtpPassword,
            @RequestParam(required = false) String googleClientId, @RequestParam(required = false) String googleClientSecret,
            @RequestParam(required = false) String googleRedirectUri, @RequestParam(required = false) String captchaSecret,
            @RequestParam(required = false) String captchaSiteKey,
            @RequestParam(required = false) MultipartFile logo, @RequestParam(required = false) MultipartFile favicon) {
        return adminSettingsService.updateAppInfo(new AdminAppInfoRequest(siteName, siteTitle, siteAddress, description,
                keywords, timezone, welcomeMessage, googleTranslateEnabled, tradeMode, tradingWinrate, merchantKey,
                contactEmail, defaultCurrencySymbol, weekendTradeEnabled, enableEmailVerification, enableKyc,
                enableKycRegistration, captchaProvider, socialLoginEnabled, returnCapital, shouldCancelPlan,
                mailServer, mailFromAddress, mailFromName, smtpHost, smtpPort, smtpEncryption, smtpUsername,
                smtpPassword, googleClientId, googleClientSecret, googleRedirectUri, captchaSecret, captchaSiteKey),
                logo, favicon);
    }

    @PutMapping("/referral")
    public AdminSettingsSummary updateReferral(@RequestBody AdminReferralSettingsRequest request) {
        return adminSettingsService.updateReferral(request);
    }

    @PutMapping("/subscription")
    public AdminSettingsSummary updateSubscription(@RequestBody AdminSubscriptionSettingsRequest request) {
        return adminSettingsService.updateSubscription(request);
    }

    @PutMapping("/payment")
    public AdminSettingsSummary updatePayment(@RequestBody AdminPaymentSettingsRequest request) {
        return adminSettingsService.updatePayment(request);
    }

    @PutMapping("/crypto")
    public AdminSettingsSummary updateCrypto(@RequestBody AdminCryptoSettingsRequest request) {
        return adminSettingsService.updateCrypto(request);
    }

    @GetMapping("/payment-methods")
    public List<AdminWithdrawalMethodSummary> listMethods() {
        return adminSettingsService.listMethods();
    }

    @PostMapping(value = "/payment-methods", consumes = "multipart/form-data")
    public AdminWithdrawalMethodSummary createMethod(
            @RequestParam String name, @RequestParam(required = false) String methodType, @RequestParam String type,
            @RequestParam(required = false) BigDecimal minimumAmount, @RequestParam(required = false) BigDecimal maximumAmount,
            @RequestParam(required = false) BigDecimal chargesAmount, @RequestParam(required = false) String chargesType,
            @RequestParam(required = false) String durationNote, @RequestParam(required = false) String imageUrl,
            @RequestParam(required = false) String bankName, @RequestParam(required = false) String accountName,
            @RequestParam(required = false) String accountNumber, @RequestParam(required = false) String swiftCode,
            @RequestParam(required = false) String walletAddress, @RequestParam(required = false) String network,
            @RequestParam String status, @RequestParam(required = false) MultipartFile barcode) {
        return adminSettingsService.createMethod(new AdminWithdrawalMethodRequest(name, methodType, type, minimumAmount,
                maximumAmount, chargesAmount, chargesType, durationNote, imageUrl, bankName, accountName,
                accountNumber, swiftCode, walletAddress, network, status), barcode);
    }

    @PutMapping(value = "/payment-methods/{id}", consumes = "multipart/form-data")
    public AdminWithdrawalMethodSummary updateMethod(
            @PathVariable Long id,
            @RequestParam String name, @RequestParam(required = false) String methodType, @RequestParam String type,
            @RequestParam(required = false) BigDecimal minimumAmount, @RequestParam(required = false) BigDecimal maximumAmount,
            @RequestParam(required = false) BigDecimal chargesAmount, @RequestParam(required = false) String chargesType,
            @RequestParam(required = false) String durationNote, @RequestParam(required = false) String imageUrl,
            @RequestParam(required = false) String bankName, @RequestParam(required = false) String accountName,
            @RequestParam(required = false) String accountNumber, @RequestParam(required = false) String swiftCode,
            @RequestParam(required = false) String walletAddress, @RequestParam(required = false) String network,
            @RequestParam String status, @RequestParam(required = false) MultipartFile barcode) {
        return adminSettingsService.updateMethod(id, new AdminWithdrawalMethodRequest(name, methodType, type, minimumAmount,
                maximumAmount, chargesAmount, chargesType, durationNote, imageUrl, bankName, accountName,
                accountNumber, swiftCode, walletAddress, network, status), barcode);
    }

    @PostMapping("/payment-methods/{id}/toggle")
    public void toggleMethod(@PathVariable Long id) {
        adminSettingsService.toggleMethodStatus(id);
    }

    @DeleteMapping("/payment-methods/{id}")
    public void deleteMethod(@PathVariable Long id) {
        adminSettingsService.deleteMethod(id);
    }
}
