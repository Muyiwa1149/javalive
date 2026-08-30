package com.javalive.backend.web.settings;

import com.javalive.backend.dto.settings.ContactRequest;
import com.javalive.backend.dto.settings.FaqSummary;
import com.javalive.backend.dto.settings.TestimonySummary;
import com.javalive.backend.entity.AppSetting;
import com.javalive.backend.entity.TermsPrivacy;
import com.javalive.backend.repository.FaqRepository;
import com.javalive.backend.repository.TermsPrivacyRepository;
import com.javalive.backend.repository.TestimonyRepository;
import com.javalive.backend.service.mail.MailService;
import com.javalive.backend.service.settings.SettingsService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** Read endpoints for the CMS-managed marketing content (FAQ, testimonials, privacy policy) + the public contact form. */
@RestController
@RequestMapping("/api/public")
public class PublicContentController {

    private final FaqRepository faqRepository;
    private final TestimonyRepository testimonyRepository;
    private final TermsPrivacyRepository termsPrivacyRepository;
    private final SettingsService settingsService;
    private final MailService mailService;

    public PublicContentController(FaqRepository faqRepository, TestimonyRepository testimonyRepository,
                                    TermsPrivacyRepository termsPrivacyRepository, SettingsService settingsService,
                                    MailService mailService) {
        this.faqRepository = faqRepository;
        this.testimonyRepository = testimonyRepository;
        this.termsPrivacyRepository = termsPrivacyRepository;
        this.settingsService = settingsService;
        this.mailService = mailService;
    }

    @GetMapping("/faqs")
    public List<FaqSummary> faqs() {
        return faqRepository.findAll(org.springframework.data.domain.Sort.by(
                        org.springframework.data.domain.Sort.Direction.DESC, "id"))
                .stream().map(FaqSummary::from).toList();
    }

    @GetMapping("/testimonials")
    public List<TestimonySummary> testimonials() {
        return testimonyRepository.findAll().stream().map(TestimonySummary::from).toList();
    }

    @GetMapping("/privacy-policy")
    public Map<String, String> privacyPolicy() {
        String description = termsPrivacyRepository.findById(1L).map(TermsPrivacy::getDescription).orElse("");
        return Map.of("description", description);
    }

    @PostMapping("/contact")
    public Map<String, String> contact(@Valid @RequestBody ContactRequest request) {
        AppSetting settings = settingsService.get();
        String subject = "Inquiry from " + request.name() + " with email " + request.email() + ": " + request.subject();
        mailService.send(settings.getContactEmail(), subject, request.message());
        return Map.of("message", "Your message was sent successfully!");
    }
}
