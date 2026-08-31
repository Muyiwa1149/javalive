package com.javalive.backend.service.admin;

import com.javalive.backend.dto.admin.AdminKycSummary;
import com.javalive.backend.entity.Kyc;
import com.javalive.backend.entity.User;
import com.javalive.backend.repository.KycRepository;
import com.javalive.backend.repository.UserRepository;
import com.javalive.backend.service.mail.MailService;
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

    public AdminKycService(KycRepository kycRepository, UserRepository userRepository,
                            FileStorageService fileStorageService, MailService mailService) {
        this.kycRepository = kycRepository;
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
        this.mailService = mailService;
    }

    @Transactional(readOnly = true)
    public List<AdminKycSummary> list() {
        return kycRepository.findAllByOrderByIdDesc().stream().map(AdminKycSummary::from).toList();
    }

    @Transactional
    public void decide(Long id, String action, String subject, String message) {
        Kyc kyc = kycRepository.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "KYC application not found."));
        User user = kyc.getUser();

        if ("Accept".equals(action)) {
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

        if (subject != null && !subject.isBlank() && message != null && !message.isBlank()) {
            mailService.send(user.getEmail(), subject, message);
        }
    }
}
