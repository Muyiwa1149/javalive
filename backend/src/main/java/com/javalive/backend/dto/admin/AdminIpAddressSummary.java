package com.javalive.backend.dto.admin;

import com.javalive.backend.entity.IpAddress;

import java.time.LocalDateTime;

public record AdminIpAddressSummary(Long id, String ipAddress, LocalDateTime createdAt) {
    public static AdminIpAddressSummary from(IpAddress ip) {
        return new AdminIpAddressSummary(ip.getId(), ip.getIpAddress(), ip.getCreatedAt());
    }
}
