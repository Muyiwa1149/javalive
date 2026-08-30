package com.javalive.backend.web.user;

import com.javalive.backend.dto.user.ProfileDetail;
import com.javalive.backend.dto.user.UpdateEmailPreferencesRequest;
import com.javalive.backend.dto.user.UpdatePayoutInfoRequest;
import com.javalive.backend.dto.user.UpdateProfileRequest;
import com.javalive.backend.dto.user.UpdateUserPasswordRequest;
import com.javalive.backend.security.UserPrincipal;
import com.javalive.backend.service.user.ProfileService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    public ProfileDetail get(@AuthenticationPrincipal UserPrincipal principal) {
        return profileService.get(principal.getId());
    }

    @PutMapping
    public ProfileDetail updateProfile(@AuthenticationPrincipal UserPrincipal principal, @Valid @RequestBody UpdateProfileRequest request) {
        return profileService.updateProfile(principal.getId(), request);
    }

    @PutMapping("/payout-info")
    public ProfileDetail updatePayoutInfo(@AuthenticationPrincipal UserPrincipal principal, @RequestBody UpdatePayoutInfoRequest request) {
        return profileService.updatePayoutInfo(principal.getId(), request);
    }

    @PutMapping("/password")
    public Map<String, String> updatePassword(@AuthenticationPrincipal UserPrincipal principal, @Valid @RequestBody UpdateUserPasswordRequest request) {
        profileService.updatePassword(principal.getId(), request);
        return Map.of("message", "Password updated successfully.");
    }

    @PutMapping("/email-preferences")
    public ProfileDetail updateEmailPreferences(@AuthenticationPrincipal UserPrincipal principal, @RequestBody UpdateEmailPreferencesRequest request) {
        return profileService.updateEmailPreferences(principal.getId(), request);
    }
}
