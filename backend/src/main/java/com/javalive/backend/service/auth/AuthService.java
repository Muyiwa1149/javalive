package com.javalive.backend.service.auth;

import com.javalive.backend.dto.auth.AuthResponse;
import com.javalive.backend.dto.auth.LoginRequest;
import com.javalive.backend.dto.auth.RegisterRequest;
import com.javalive.backend.dto.auth.UserSummary;
import com.javalive.backend.entity.CryptoAccount;
import com.javalive.backend.entity.User;
import com.javalive.backend.repository.CryptoAccountRepository;
import com.javalive.backend.repository.UserRepository;
import com.javalive.backend.security.JwtService;
import com.javalive.backend.web.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final CryptoAccountRepository cryptoAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, CryptoAccountRepository cryptoAccountRepository,
                        PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.cryptoAccountRepository = cryptoAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ApiException(HttpStatus.CONFLICT, "An account with this email already exists.");
        }

        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .phone(request.phone())
                .country(request.country())
                .referredByCode(request.refBy())
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

        String token = jwtService.generateToken(user.getId(), "USER", user.getEmail());
        return new AuthResponse(token, UserSummary.from(user));
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password."));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BadCredentialsException("Invalid email or password.");
        }
        if ("blocked".equalsIgnoreCase(user.getStatus())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "This account has been blocked.");
        }

        String token = jwtService.generateToken(user.getId(), "USER", user.getEmail());
        return new AuthResponse(token, UserSummary.from(user));
    }

    public UserSummary me(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found."));
        return UserSummary.from(user);
    }
}
