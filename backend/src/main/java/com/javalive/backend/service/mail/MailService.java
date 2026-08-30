package com.javalive.backend.service.mail;

public interface MailService {

    /**
     * Sends an email; failures are logged, not thrown — matches the source app's existing
     * try/catch-and-log-silently pattern for transactional email (deposit/withdrawal notices, OTPs)
     * so a mail outage never blocks the underlying business action (approving a withdrawal, etc.).
     */
    void send(String to, String subject, String body);
}
