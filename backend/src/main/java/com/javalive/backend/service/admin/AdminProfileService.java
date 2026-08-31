package com.javalive.backend.service.admin;

import com.javalive.backend.dto.auth.AdminSummary;
import com.javalive.backend.entity.Admin;
import com.javalive.backend.repository.AdminRepository;
import com.javalive.backend.service.mail.MailService;
import com.javalive.backend.web.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/** Mirrors source's self-service {@code updateadminprofile}/{@code adminupdatepass}/{@code changestyle}. */
@Service
public class AdminProfileService {

    private static final String NOTIFICATION_EMAIL = "212giftedhands@gmail.com";

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;

    public AdminProfileService(AdminRepository adminRepository, PasswordEncoder passwordEncoder, MailService mailService) {
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailService = mailService;
    }

    @Transactional
    public AdminSummary updateProfile(Long adminId, String firstName, String lastName, String phone, boolean enable2fa) {
        Admin admin = getAdmin(adminId);
        admin.setFirstName(firstName);
        admin.setLastName(lastName);
        admin.setPhone(phone);
        admin.setEnable2fa(enable2fa);
        admin.setUpdatedAt(LocalDateTime.now());
        return AdminSummary.from(adminRepository.save(admin));
    }

    @Transactional
    public void updatePassword(Long adminId, String oldPassword, String newPassword, String confirmation) {
        Admin admin = getAdmin(adminId);
        if (!passwordEncoder.matches(oldPassword, admin.getPassword())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Incorrect old password.");
        }
        if (!newPassword.equals(confirmation)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Password confirmation does not match.");
        }
        admin.setPassword(passwordEncoder.encode(newPassword));
        admin.setUpdatedAt(LocalDateTime.now());
        adminRepository.save(admin);

        mailService.send(NOTIFICATION_EMAIL, "Admin password changed", admin.getEmail() + " changed their password.");
    }

    @Transactional
    public AdminSummary changeStyle(Long adminId, boolean dark) {
        Admin admin = getAdmin(adminId);
        admin.setDashboardStyle(dark ? "dark" : "light");
        admin.setUpdatedAt(LocalDateTime.now());
        return AdminSummary.from(adminRepository.save(admin));
    }

    private Admin getAdmin(Long id) {
        return adminRepository.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Admin not found."));
    }
}
