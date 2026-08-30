package com.javalive.backend.service.referral;

import com.javalive.backend.dto.referral.DownlineMember;
import com.javalive.backend.dto.referral.ReferralOverview;
import com.javalive.backend.entity.User;
import com.javalive.backend.repository.UserRepository;
import com.javalive.backend.service.settings.SettingsService;
import com.javalive.backend.web.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Mirrors the source app's referral downline tree (Controller::getdownlines / UsersController's
 * copy) — walks the referred_by_code chain recursively, bounded at 6 levels deep (same cutoff the
 * source used). Unlike the source, which fetched {@code User::all()} into memory and filtered in
 * Blade, this queries only the users actually in each level's chain.
 */
@Service
public class ReferralService {

    private static final int MAX_DEPTH = 6;

    private final UserRepository userRepository;
    private final SettingsService settingsService;

    public ReferralService(UserRepository userRepository, SettingsService settingsService) {
        this.userRepository = userRepository;
        this.settingsService = settingsService;
    }

    @Transactional(readOnly = true)
    public ReferralOverview overview(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found."));

        List<DownlineMember> downline = new ArrayList<>();
        long directCount = buildDownline(user, 0, downline);

        String referralLink = user.getUsername() != null ? "/ref/" + user.getUsername() : null;

        return new ReferralOverview(referralLink, user.getUsername(), directCount,
                user.getReferralBonusBalance(), settingsService.get().getReferralCommissionPct(), downline);
    }

    private long buildDownline(User parent, int level, List<DownlineMember> out) {
        if (level >= MAX_DEPTH) {
            return 0;
        }
        List<User> children = userRepository.findByReferredByCode(parent.getId().toString());
        for (User child : children) {
            String levelLabel = level == 0 ? "Direct referral" : "Indirect referral level " + level;
            out.add(new DownlineMember(child.getId(), child.getName(), level, levelLabel,
                    parent.getName(), child.getStatus(), child.getCreatedAt()));
            buildDownline(child, level + 1, out);
        }
        return children.size();
    }
}
