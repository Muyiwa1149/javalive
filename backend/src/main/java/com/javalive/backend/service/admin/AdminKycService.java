package com.javalive.backend.service.admin;

import com.javalive.backend.dto.admin.AdminKycSummary;
import com.javalive.backend.entity.Kyc;
import com.javalive.backend.entity.User;
import com.javalive.backend.repository.KycRepository;
import com.javalive.backend.repository.UserRepository;
import com.javalive.backend.service.mail.MailService;
import com.javalive.backend.service.notification.NotificationService;
import com.javalive.backend.service.storage.FileStorageService;
import com.javalive.backend.web.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/** Mirrors the source app's {@code Admin\KycController@processKyc} exactly. */
@Service
public class AdminKycService {

    private final KycRepository kycRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final MailService mailService;
    private final NotificationService notificationService;

    public AdminKycService(KycRepository kycRepository, UserRepository userRepository,
                            FileStorageService fileStorageService, MailService mailService,
                            NotificationService notificationService) {
        this.kycRepository = kycRepository;
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
        this.mailService = mailService;
        this.notificationService = notificationService;
    }

    @Transactional(readOnly = true)
    public List<AdminKycSummary> list() {
        return kycRepository.findAllByOrderByIdDesc().stream().map(AdminKycSummary::from).toList();
    }

    @Transactional
    public void decide(Long id, String action, String subject, String message) {
        Kyc kyc = kycRepository.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "KYC application not found."));
        User user = kyc.getUser();
        boolean accepted = "Accept".equals(action);

        if (accepted) {
            user.setAccountVerifyStatus("Verified");
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
            kyc.setStatus("Verified");
            kyc.setUpdatedAt(LocalDateTime.now());
            kycRepository.save(kyc);
        } else {
            fileStorageService.delete(kyc.getFrontImage());
            fileStorageService.delete(kyc.getBackImage());
            user.setAccountVerifyStatus("Rejected");
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
            kycRepository.delete(kyc);
        }

        // Always notify+email with a sensible default, regardless of whether the admin supplied a
        // custom subject/message — previously this silently sent nothing if either was left blank.
        String finalSubject = (subject != null && !subject.isBlank()) ? subject
                : (accepted ? "Identity Verified" : "Identity Verification Rejected");
        String finalMessage = (message != null && !message.isBlank()) ? message
                : (accepted
                    ? "Your identity verification has been approved. You now have full access to all trading features."
                    : "Your identity verification could not be approved. Please resubmit clear, valid documents.");

        notificationService.notifyUser(user, finalSubject, finalMessage, accepted ? "success" : "danger", kyc.getId(), "kyc");
        mailService.send(user.getEmail(), finalSubject, finalMessage);
    }
}
