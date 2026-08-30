package com.javalive.backend.web.kyc;

import com.javalive.backend.dto.kyc.KycStatusResponse;
import com.javalive.backend.security.UserPrincipal;
import com.javalive.backend.service.kyc.KycService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/kyc")
public class KycController {

    private final KycService kycService;

    public KycController(KycService kycService) {
        this.kycService = kycService;
    }

    @GetMapping("/status")
    public KycStatusResponse status(@AuthenticationPrincipal UserPrincipal principal) {
        return kycService.getStatus(principal.getId());
    }

    @PostMapping(consumes = "multipart/form-data")
    public KycStatusResponse submit(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam String email,
            @RequestParam String phoneNumber,
            @RequestParam String dob,
            @RequestParam(required = false) String socialMedia,
            @RequestParam String address,
            @RequestParam String city,
            @RequestParam String state,
            @RequestParam String country,
            @RequestParam String documentType,
            @RequestParam MultipartFile frontImg,
            @RequestParam MultipartFile backImg) {
        return kycService.submit(principal.getId(), firstName, lastName, email, phoneNumber, dob, socialMedia,
                address, city, state, country, documentType, frontImg, backImg);
    }
}
