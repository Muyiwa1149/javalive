package com.javalive.backend.service.loan;

import com.javalive.backend.dto.loan.LoanApplicationRequest;
import com.javalive.backend.dto.loan.LoanSummary;
import com.javalive.backend.entity.Loan;
import com.javalive.backend.entity.User;
import com.javalive.backend.repository.LoanRepository;
import com.javalive.backend.repository.UserRepository;
import com.javalive.backend.service.mail.MailService;
import com.javalive.backend.service.settings.SettingsService;
import com.javalive.backend.web.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/** Mirrors the source app's LoanController — application only, no self-service approval (that's admin-side, Phase 5). */
@Service
public class LoanService {

    private final LoanRepository loanRepository;
    private final UserRepository userRepository;
    private final MailService mailService;
    private final SettingsService settingsService;

    public LoanService(LoanRepository loanRepository, UserRepository userRepository,
                        MailService mailService, SettingsService settingsService) {
        this.loanRepository = loanRepository;
        this.userRepository = userRepository;
        this.mailService = mailService;
        this.settingsService = settingsService;
    }

    @Transactional
    public LoanSummary apply(Long userId, LoanApplicationRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found."));

        LocalDateTime now = LocalDateTime.now();
        Loan loan = Loan.builder()
                .user(user).amount(request.amount()).income(request.income()).purpose(request.purpose())
                .duration(request.duration()).facility(request.facility()).active("Pending")
                .invDuration(request.duration()).activatedAt(now).lastGrowth(now)
                .createdAt(now).updatedAt(now)
                .build();
        loan = loanRepository.save(loan);

        String contactEmail = settingsService.get().getContactEmail();
        if (contactEmail != null) {
            mailService.send(contactEmail, "Loan Application by " + user.getName(),
                    "This is to inform you that " + user.getName() + " just applied for a loan plan for " + request.purpose());
        }

        return LoanSummary.from(loan);
    }

    @Transactional(readOnly = true)
    public List<LoanSummary> myLoans(Long userId) {
        return loanRepository.findByUserIdOrderByIdDesc(userId).stream().map(LoanSummary::from).toList();
    }
}
