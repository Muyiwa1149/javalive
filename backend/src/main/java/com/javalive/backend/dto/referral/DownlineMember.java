package com.javalive.backend.dto.referral;

import java.time.LocalDateTime;

public record DownlineMember(
        Long id, String name, int level, String levelLabel, String parentName, String status, LocalDateTime registeredAt
) {
}
