package com.javalive.backend.service.user;

import com.javalive.backend.dto.user.ProfileDetail;
import com.javalive.backend.dto.user.UpdateEmailPreferencesRequest;
import com.javalive.backend.dto.user.UpdatePayoutInfoRequest;
import com.javalive.backend.dto.user.UpdateProfileRequest;
import com.javalive.backend.dto.user.UpdateUserPasswordRequest;
import com.javalive.backend.entity.User;
import com.javalive.backend.repository.UserRepository;
import com.javalive.backend.web.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/** Mirrors the source app's ProfileController — separate small updates for profile info, payout details, password, and email preferences. */
@Service
public class ProfileService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public ProfileService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public ProfileDetail get(Long userId) {
        return ProfileDetail.from(findUser(userId));
    }

    public ProfileDetail updateProfile(Long userId, UpdateProfileRequest request) {
        User user = findUser(userId);
        user.setName(request.name());
        user.setDob(request.dob());
        user.setPhone(request.phone());
        user.setAddress(request.address());
        user.setUpdatedAt(LocalDateTime.now());
        return ProfileDetail.from(userRepository.save(user));
    }

    public ProfileDetail updatePayoutInfo(Long userId, UpdatePayoutInfoRequest request) {
        User user = findUser(userId);
        user.setBankName(request.bankName());
        user.setBankAccountName(request.bankAccountName());
        user.setBankAccountNumber(request.bankAccountNumber());
        user.setBankSwiftCode(request.bankSwiftCode());
        user.setBtcAddress(request.btcAddress());
        user.setEthAddress(request.ethAddress());
        user.setLtcAddress(request.ltcAddress());
        user.setUsdtAddress(request.usdtAddress());
        user.setUpdatedAt(LocalDateTime.now());
        return ProfileDetail.from(userRepository.save(user));
    }

    public void updatePassword(Long userId, UpdateUserPasswordRequest request) {
        User user = findUser(userId);
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Current password does not match.");
        }
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    public ProfileDetail updateEmailPreferences(Long userId, UpdateEmailPreferencesRequest request) {
        User user = findUser(userId);
        user.setSendOtpEmail(request.sendOtpEmail());
        user.setSendRoiEmail(request.sendRoiEmail());
        user.setSendInvPlanEmail(request.sendInvPlanEmail());
        user.setUpdatedAt(LocalDateTime.now());
        return ProfileDetail.from(userRepository.save(user));
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found."));
    }
}
