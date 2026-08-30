package com.javalive.backend.security.ipblock;

import com.javalive.backend.service.settings.IpBlacklistService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/** Rejects requests from admin-blacklisted IPs before they reach any controller — mirrors the source app's `BlockIpAddressMiddleware`. */
@Component
public class IpBlacklistFilter extends OncePerRequestFilter {

    private final IpBlacklistService ipBlacklistService;

    public IpBlacklistFilter(IpBlacklistService ipBlacklistService) {
        this.ipBlacklistService = ipBlacklistService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                     @NonNull FilterChain filterChain) throws ServletException, IOException {
        String clientIp = resolveClientIp(request);
        if (ipBlacklistService.isBlocked(clientIp)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write("{\"status\":403,\"message\":\"Access denied.\"}");
            return;
        }
        filterChain.doFilter(request, response);
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
