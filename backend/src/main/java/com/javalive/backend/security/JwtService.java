package com.javalive.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {

    private final SecretKey signingKey;
    private final long accessTokenMinutes;

    public JwtService(
            @Value("${javalive.jwt.secret}") String secret,
            @Value("${javalive.jwt.access-token-minutes}") long accessTokenMinutes) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenMinutes = accessTokenMinutes;
    }

    public String generateToken(Long subjectId, String role, String email) {
        return generateToken(subjectId, role, email, accessTokenMinutes);
    }

    /** Overload for short-lived special-purpose tokens (e.g. the admin 2FA pending-verification step). */
    public String generateToken(Long subjectId, String role, String email, long expiryMinutes) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(subjectId))
                .claims(Map.of("role", role, "email", email))
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(expiryMinutes * 60)))
                .signWith(signingKey)
                .compact();
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Long extractSubjectId(String token) {
        return Long.valueOf(parseClaims(token).getSubject());
    }

    public String extractRole(String token) {
        return parseClaims(token).get("role", String.class);
    }
}
