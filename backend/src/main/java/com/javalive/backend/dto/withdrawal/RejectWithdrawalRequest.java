package com.javalive.backend.dto.withdrawal;

public record RejectWithdrawalRequest(String reason, String subject, boolean sendEmail) {
}
