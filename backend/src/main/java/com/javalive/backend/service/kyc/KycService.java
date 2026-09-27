package com.javalive.backend.service.kyc;

import com.javalive.backend.dto.kyc.KycStatusResponse;
import com.javalive.backend.entity.Kyc;
import com.javalive.backend.entity.User;
import com.javalive.backend.repository.KycRepository;
import com.javalive.backend.repository.UserRepository;
import com.javalive.backend.service.notification.NotificationService;
import com.javalive.backend.service.storage.FileStorageService;
import com.javalive.backend.web.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

/** Mirrors the source app's VerifyController — one KYC submission per user, admin-reviewed. */
@Service
public class KycService {

    private static final List<String> ALLOWED_IMAGE_EXTENSIONS = List.of("jpg", "jpeg", "png");

    private final KycRepository kycRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final NotificationService notificationService;

    public KycService(KycRepository kycRepository, UserRepository userRepository, FileStorageService fileStorageService,
                       NotificationService notificationService) {
        this.kycRepository = kycRepository;
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
        this.notificationService = notificationService;
    }

    public KycStatusResponse getStatus(Long userId) {
        return kycRepository.findTopByUserIdOrderByIdDesc(userId)
                .map(KycStatusResponse::from)
                .orElse(KycStatusResponse.notSubmitted());
    }

    public KycStatusResponse submit(Long userId, String firstName, String lastName, String email, String phoneNumber,
                                     String dob, String socialMedia, String address, String city, String state,
                                     String country, String documentType, MultipartFile frontImg, MultipartFile backImg) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found."));

        String frontPath = fileStorageService.storeImage(frontImg, "uploads", ALLOWED_IMAGE_EXTENSIONS);
        String backPath = fileStorageService.storeImage(backImg, "uploads", ALLOWED_IMAGE_EXTENSIONS);

        Kyc kyc = Kyc.builder()
                .user(user).firstName(firstName).lastName(lastName).email(email).phoneNumber(phoneNumber)
                .dob(dob).socialMedia(socialMedia == null || socialMedia.isBlank() ? "Not provided" : socialMedia)
                .address(address).city(city).state(state).country(country).documentType(documentType)
                .frontImage(frontPath).backImage(backPath).status("Under review")
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();
        kyc = kycRepository.save(kyc);

        user.setAccountVerifyStatus("Under review");
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        notificationService.emailAllAdmins("Identity Verification Request from " + user.getName(),
                "This is to inform you that " + user.getName() + " just submitted a request for KYC (identity verification), please login your admin account to review and take necessary action.");

        return KycStatusResponse.from(kyc);
    }
}
