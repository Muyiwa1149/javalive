package com.javalive.backend.service.mail;

import com.javalive.backend.service.settings.SettingsService;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/**
 * Renders every outbound email through the same branded HTML template the source app's
 * {@code NewNotification} Mailable used for essentially all of its transactional email — mirrors
 * that "one shared branded template, plain-text body swapped in" approach rather than the many
 * bespoke Mailables/Blade views source had for narrower cases (2FA, ROI, deposit/withdrawal status,
 * etc.), since none of those added content beyond what the shared template + subject already convey.
 */
@Service
public class SmtpMailService implements MailService {

    private static final Logger log = LoggerFactory.getLogger(SmtpMailService.class);

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final SettingsService settingsService;

    @Value("${spring.mail.from:no-reply@javalive.local}")
    private String fromAddress;

    public SmtpMailService(JavaMailSender mailSender, TemplateEngine templateEngine, SettingsService settingsService) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.settingsService = settingsService;
    }

    @Override
    public void send(String to, String subject, String body) {
        try {
            var settings = settingsService.get();
            Context context = new Context();
            context.setVariable("subject", subject);
            context.setVariable("body", body);
            context.setVariable("siteName", settings.getSiteName() != null ? settings.getSiteName() : "Keystone Bit-Forex");
            context.setVariable("contactEmail", settings.getContactEmail() != null ? settings.getContactEmail() : fromAddress);
            String html = templateEngine.process("email/notification", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
        } catch (Exception e) {
            log.warn("Failed to send email to {} (subject: {}): {}", to, subject, e.getMessage());
        }
    }
}
