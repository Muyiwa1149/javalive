package com.javalive.backend.service.admin;

import com.javalive.backend.entity.Mt4Detail;
import com.javalive.backend.repository.Mt4DetailRepository;
import com.javalive.backend.service.mail.MailService;
import com.javalive.backend.service.settings.SettingsService;
import com.javalive.backend.web.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/** Local (non-external-API) half of {@code Admin\TradingAccountController@confirmsub}. */
@Service
public class AdminTradingAccountService {

    private final Mt4DetailRepository mt4DetailRepository;
    private final MailService mailService;
    private final SettingsService settingsService;

    public AdminTradingAccountService(Mt4DetailRepository mt4DetailRepository, MailService mailService,
                                       SettingsService settingsService) {
        this.mt4DetailRepository = mt4DetailRepository;
        this.mailService = mailService;
        this.settingsService = settingsService;
    }

    @Transactional
    public void confirmSubscription(Long id) {
        Mt4Detail sub = mt4DetailRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Subscription not found."));

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endAt = switch (sub.getDuration() == null ? "" : sub.getDuration()) {
            case "Monthly" -> now.plusMonths(1);
            case "Quaterly" -> now.plusMonths(4);
            case "Yearly" -> now.plusYears(1);
            default -> now.plusMonths(1);
        };

        sub.setStartDate(now);
        sub.setEndDate(endAt);
        sub.setRemindedAt(endAt.minusDays(10));
        sub.setStatus("Active");
        sub.setUpdatedAt(now);
        mt4DetailRepository.save(sub);

        if (sub.getUser() != null) {
            mailService.send(sub.getUser().getEmail(), "Subscription Account Started!",
                    sub.getUser().getName() + ", this is to inform you that your trading account management "
                            + "request has been reviewed and processed. Thank you for trusting "
                            + settingsService.get().getSiteName());
        }
    }
}
