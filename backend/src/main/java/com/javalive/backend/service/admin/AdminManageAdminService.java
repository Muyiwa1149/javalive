package com.javalive.backend.service.admin;

import com.javalive.backend.dto.admin.AdminCreateManagerRequest;
import com.javalive.backend.dto.admin.AdminEditManagerRequest;
import com.javalive.backend.dto.admin.AdminManagerSummary;
import com.javalive.backend.entity.Admin;
import com.javalive.backend.repository.AdminRepository;
import com.javalive.backend.service.mail.MailService;
import com.javalive.backend.web.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Mirrors the source app's {@code Admin\ManageAdminController} CRUD (blockadmin/unblockadmin/
 * deleteadminacnt/editadmin/saveadmin), mapping the dead {@code acnt_type_active} column to the
 * real {@code status} field (consistent with {@code AdminAuthService}'s active-login check).
 *
 * <p><b>Security hardening beyond source</b>: in the PHP app, "Super Admin"-only is a purely
 * client-side sidebar gate (see {@code sidebar.blade.php}) — none of these controller actions
 * re-check {@code type} server-side, so any authenticated admin could call these routes directly.
 * Every method here re-verifies the caller is a Super Admin before acting.
 */
@Service
public class AdminManageAdminService {

    private static final String SUPER_ADMIN = "Super Admin";
    private static final String NOTIFICATION_EMAIL = "212giftedhands@gmail.com";

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;

    public AdminManageAdminService(AdminRepository adminRepository, PasswordEncoder passwordEncoder, MailService mailService) {
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailService = mailService;
    }

    @Transactional(readOnly = true)
    public List<AdminManagerSummary> list(Admin actingAdmin) {
        requireSuperAdmin(actingAdmin);
        return adminRepository.findAll().stream().map(AdminManagerSummary::from).toList();
    }

    @Transactional
    public AdminManagerSummary create(Admin actingAdmin, AdminCreateManagerRequest request) {
        requireSuperAdmin(actingAdmin);
        if (adminRepository.existsByEmail(request.email())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "An admin with this email already exists.");
        }
        Admin admin = Admin.builder()
                .firstName(request.firstName()).lastName(request.lastName()).email(request.email())
                .phone(request.phone()).type(request.type()).status("active").dashboardStyle("light")
                .enable2fa(false).pass2fa(false)
                .password(passwordEncoder.encode(request.password()))
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();
        admin = adminRepository.save(admin);

        mailService.send(NOTIFICATION_EMAIL, "New admin created",
                "Email: " + request.email() + "  password: " + request.password());
        return AdminManagerSummary.from(admin);
    }

    @Transactional
    public AdminManagerSummary edit(Admin actingAdmin, Long id, AdminEditManagerRequest request) {
        requireSuperAdmin(actingAdmin);
        Admin admin = getAdmin(id);
        admin.setFirstName(request.firstName());
        admin.setLastName(request.lastName());
        admin.setEmail(request.email());
        admin.setPhone(request.phone());
        admin.setType(request.type());
        admin.setUpdatedAt(LocalDateTime.now());
        admin = adminRepository.save(admin);

        mailService.send(NOTIFICATION_EMAIL, "Admin email updated", "New email: " + request.email());
        return AdminManagerSummary.from(admin);
    }

    @Transactional
    public void block(Admin actingAdmin, Long id) {
        requireSuperAdmin(actingAdmin);
        Admin admin = getAdmin(id);
        admin.setStatus("blocked");
        admin.setUpdatedAt(LocalDateTime.now());
        adminRepository.save(admin);
    }

    @Transactional
    public void unblock(Admin actingAdmin, Long id) {
        requireSuperAdmin(actingAdmin);
        Admin admin = getAdmin(id);
        admin.setStatus("active");
        admin.setUpdatedAt(LocalDateTime.now());
        adminRepository.save(admin);
    }

    @Transactional
    public void delete(Admin actingAdmin, Long id) {
        requireSuperAdmin(actingAdmin);
        if (actingAdmin.getId().equals(id)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "You cannot delete your own account.");
        }
        adminRepository.delete(getAdmin(id));
    }

    @Transactional
    public void sendMail(Admin actingAdmin, Long id, String subject, String message) {
        requireSuperAdmin(actingAdmin);
        Admin admin = getAdmin(id);
        mailService.send(admin.getEmail(), subject, message);
    }

    private Admin getAdmin(Long id) {
        return adminRepository.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Admin not found."));
    }

    private void requireSuperAdmin(Admin actingAdmin) {
        if (!SUPER_ADMIN.equals(actingAdmin.getType())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Only Super Admins can manage administrator accounts.");
        }
    }
}
