package com.javalive.backend.service.auth;

import com.javalive.backend.dto.auth.AuthResponse;
import com.javalive.backend.dto.auth.LoginRequest;
import com.javalive.backend.dto.auth.RegisterRequest;
import com.javalive.backend.dto.auth.UserLoginResponse;
import com.javalive.backend.dto.auth.UserSummary;
import com.javalive.backend.entity.CryptoAccount;
import com.javalive.backend.entity.User;
import com.javalive.backend.repository.CryptoAccountRepository;
import com.javalive.backend.repository.UserRepository;
import com.javalive.backend.security.JwtService;
import com.javalive.backend.service.mail.MailService;
import com.javalive.backend.service.twofactor.TwoFactorService;
import com.javalive.backend.web.exception.ApiException;
import io.jsonwebtoken.JwtException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class AuthService {

    private static final int PENDING_2FA_EXPIRY_MINUTES = 10;

    private final UserRepository userRepository;
    private final CryptoAccountRepository cryptoAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final MailService mailService;
    private final TwoFactorService twoFactorService;

    public AuthService(UserRepository userRepository, CryptoAccountRepository cryptoAccountRepository,
                        PasswordEncoder passwordEncoder, JwtService jwtService, MailService mailService,
                        TwoFactorService twoFactorService) {
        this.userRepository = userRepository;
        this.cryptoAccountRepository = cryptoAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.mailService = mailService;
        this.twoFactorService = twoFactorService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ApiException(HttpStatus.CONFLICT, "An account with this email already exists.");
        }
        if (userRepository.existsByUsername(request.username())) {
            throw new ApiException(HttpStatus.CONFLICT, "This username is already taken.");
        }

        // Source app's referral link (/ref/{username}) captures the sponsor's username; registration
        // resolves it to the sponsor's numeric id, which is what referral-chain/commission logic
        // actually keys on (see Controller::ref / CreateNewUser in the source app).
        String referredByCode = null;
        if (request.refBy() != null && !request.refBy().isBlank()) {
            referredByCode = userRepository.findByUsername(request.refBy())
                    .map(sponsor -> sponsor.getId().toString())
                    .orElse(null);
        }

        User user = User.builder()
                .name(request.name())
                .username(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .phone(request.phone())
                .country(request.country())
                .referredByCode(referredByCode)
                .currencySymbol("$")
                .currencyCode("USD")
                .tradeType("Profit")
                .numberOfTrades(2)
                .dashboardStyle("light")
                .accountBalance(BigDecimal.ZERO)
                .roiBalance(BigDecimal.ZERO)
                .bonusBalance(BigDecimal.ZERO)
                .referralBonusBalance(BigDecimal.ZERO)
                .signupBonus(BigDecimal.ZERO)
                .bonusReleased(false)
                .taxType("off")
                .status("active")
                .tradeMode("on")
                .sendOtpEmail(false)
                .sendRoiEmail(true)
                .sendPromoEmail(true)
                .sendInvPlanEmail(true)
                .signalStatus("off")
                .planStatus("off")
                .withdrawalCodeStatus("on")
                .copyTradingProgress(0)
                .walletConnected(false)
                .hasSignalSubscription(false)
                .legacyTradeField(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        user = userRepository.save(user);

        cryptoAccountRepository.save(CryptoAccount.builder()
                .user(user)
                .btc(BigDecimal.ZERO).eth(BigDecimal.ZERO).ltc(BigDecimal.ZERO).xrp(BigDecimal.ZERO)
                .link(BigDecimal.ZERO).bnb(BigDecimal.ZERO).aave(BigDecimal.ZERO).usdt(BigDecimal.ZERO)
                .xlm(BigDecimal.ZERO).bch(BigDecimal.ZERO).ada(BigDecimal.ZERO)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build());

        mailService.send(user.getEmail(), "Welcome to " + user.getName() + "'s new trading account",
                "Hi " + user.getName() + ",\n\nWelcome aboard! Your account has been created successfully and you're ready to start trading.\n\nUsername: " + user.getUsername());

        String token = jwtService.generateToken(user.getId(), "USER", user.getEmail());
        return new AuthResponse(token, UserSummary.from(user));
    }

    public UserLoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password."));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BadCredentialsException("Invalid email or password.");
        }
        if ("blocked".equalsIgnoreCase(user.getStatus())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "This account has been blocked.");
        }

        if (user.getTwoFactorConfirmedAt() != null) {
            String pendingToken = jwtService.generateToken(user.getId(), "USER_2FA_PENDING", user.getEmail(),
                    PENDING_2FA_EXPIRY_MINUTES);
            return new UserLoginResponse(true, pendingToken, null);
        }

        String token = jwtService.generateToken(user.getId(), "USER", user.getEmail());
        return new UserLoginResponse(false, token, UserSummary.from(user));
    }

    public UserLoginResponse verifyTwoFactor(String pendingToken, String code) {
        Long userId;
        try {
            if (!jwtService.isValid(pendingToken) || !"USER_2FA_PENDING".equals(jwtService.extractRole(pendingToken))) {
                throw new ApiException(HttpStatus.UNAUTHORIZED, "Your verification session has expired. Please log in again.");
            }
            userId = jwtService.extractSubjectId(pendingToken);
        } catch (JwtException e) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Your verification session has expired. Please log in again.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Account not found."));

        if (user.getTwoFactorConfirmedAt() == null || !twoFactorService.verifyLoginChallenge(user, code)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid verification code.");
        }

        String token = jwtService.generateToken(user.getId(), "USER", user.getEmail());
        return new UserLoginResponse(false, token, UserSummary.from(user));
    }

    public UserSummary me(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found."));
        return UserSummary.from(user);
    }
}
